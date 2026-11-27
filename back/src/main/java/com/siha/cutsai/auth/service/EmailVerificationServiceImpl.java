package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.EmailVerificationMapper.Verification;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI작성: 이메일 인증 코드와 목적별 발송 제한을 관리한다. */
@Service
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final EmailVerificationMapper mapper;
    private final MemberMapper memberMapper;
    private final PasswordEncoder encoder;
    private final ObjectProvider<EmailVerificationSender> mailSender;

    public EmailVerificationServiceImpl(EmailVerificationMapper mapper, MemberMapper memberMapper,
                                        PasswordEncoder encoder, ObjectProvider<EmailVerificationSender> mailSender) {
        this.mapper = mapper;
        this.memberMapper = memberMapper;
        this.encoder = encoder;
        this.mailSender = mailSender;
    }

    /** AI작성: 최초 발송과 반복 발송에 동일한 제한을 적용한다. */
    @Override
    @Transactional
    public void send(String email, Purpose purpose) {
        issue(email, purpose);
    }

    /** AI작성: 재발송 시 새 코드로 교체하고 기존 코드를 무효화한다. */
    @Override
    @Transactional
    public void resend(String email, Purpose purpose) {
        issue(email, purpose);
    }

    /** AI작성: 실패 횟수는 오류 응답 이후에도 유지하며 다섯 번 실패한 코드는 거부한다. */
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public void verify(String email, Purpose purpose, String code) {
        String normalized = normalize(email, purpose);
        Verification state = mapper.findForUpdate(normalized, purpose.name()).orElseThrow(this::invalidCode);
        LocalDateTime now = now();
        if (code == null || !code.matches("[0-9]{6}") || state.failedAttempts() >= 5
                || !state.expiresAt().isAfter(now)) {
            throw invalidCode();
        }
        if (!encoder.matches(code, state.codeHash())) {
            mapper.updateFailedAttempts(normalized, purpose.name(), state.failedAttempts() + 1);
            throw invalidCode();
        }
        mapper.markVerified(normalized, purpose.name(), now);
    }

    /** AI작성: 호출자의 가입·재설정 트랜잭션 안에서 인증을 확인하고 한 번만 사용한다. */
    @Override
    @Transactional
    public void consumeVerified(String email, Purpose purpose, String code) {
        String normalized = normalize(email, purpose);
        Verification state = mapper.findForUpdate(normalized, purpose.name()).orElseThrow(this::invalidCode);
        LocalDateTime now = now();
        if (code == null || !code.matches("[0-9]{6}") || state.verifiedAt() == null
                || state.failedAttempts() >= 5 || !state.expiresAt().isAfter(now)
                || !encoder.matches(code, state.codeHash())
                || mapper.consume(normalized, purpose.name(), now) != 1) {
            throw invalidCode();
        }
    }

    /** AI작성: 행 잠금으로 동시 발송을 직렬화하고 코드 수명과 발송 제한을 따로 관리한다. */
    private void issue(String email, Purpose purpose) {
        String normalized = normalize(email, purpose);
        EmailVerificationSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AUTH-MAIL-UNAVAILABLE", "메일 발송을 사용할 수 없습니다.");
        }
        if (purpose == Purpose.SIGNUP && memberMapper.existsByEmail(normalized)) {
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-003", "이미 등록된 이메일입니다.");
        }
        LocalDateTime now = now();
        String code = newCode();
        String hash = encoder.encode(code);
        mapper.insertIfAbsent(normalized, purpose.name(), hash, now.plusMinutes(5), now);
        Verification state = mapper.findForUpdate(normalized, purpose.name()).orElseThrow();
        if (!hash.equals(state.codeHash())) {
            LocalDateTime window = state.resendWindowStartedAt();
            int count = state.resendCount();
            if (window == null || !window.plusMinutes(10).isAfter(now)) {
                window = now;
                count = 0;
            }
            if (count >= 5) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AUTH-006", "재발송 제한을 초과했습니다.");
            }
            while (encoder.matches(code, state.codeHash())) {
                code = newCode();
            }
            mapper.updateCode(normalized, purpose.name(), encoder.encode(code), now.plusMinutes(5), window, count + 1);
        }
        sender.send(normalized, purpose, code);
    }

    /** AI작성: 모든 호출에서 이메일 표기와 목적을 일관되게 확인한다. */
    private String normalize(String email, Purpose purpose) {
        if (email == null || email.isBlank() || purpose == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "이메일과 인증 목적을 확인해 주세요.");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** AI작성: 추측하기 어려운 난수로 앞자리 0을 포함한 인증 코드를 만든다. */
    private String newCode() {
        return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
    }

    /** AI작성: 데이터베이스에 기록할 현재 UTC 시각을 반환한다. */
    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** AI작성: 불일치·만료·사용 불가 코드를 공통 오류로 처리한다. */
    private ApiException invalidCode() {
        return new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증 코드가 일치하지 않거나 만료되었습니다.");
    }

}
