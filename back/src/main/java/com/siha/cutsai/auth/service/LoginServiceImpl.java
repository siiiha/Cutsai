package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.AuthHash;
import com.siha.cutsai.auth.mapper.LoginRateLimitMapper;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI작성: 계정·IP 실패 제한을 적용해 로그인 자격 증명을 검증한다. */
@Service
public class LoginServiceImpl implements LoginService {
    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 3;
    private final LoginRateLimitMapper rateLimitMapper;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final String unknownMemberHash;

    public LoginServiceImpl(LoginRateLimitMapper rateLimitMapper, MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.rateLimitMapper = rateLimitMapper;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.unknownMemberHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /** AI작성: 로그인 정보를 검증하고 성공한 회원 번호를 반환한다. */
    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public long authenticate(String email, String password, String remoteAddress) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        LimitKey accountKey = new LimitKey("ACCOUNT", AuthHash.sha256(normalizedEmail));
        LimitKey ipKey = new LimitKey("IP", AuthHash.sha256(remoteAddress));
        LoginRateLimitMapper.Limit account = lockLimit(accountKey);
        LoginRateLimitMapper.Limit ip = lockLimit(ipKey);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (isLocked(account, now) || isLocked(ip, now)) throw locked();

        // AI작성: 비밀번호 확인부터 토큰 발급까지 회원 행 잠금을 유지해 재설정과 순서를 맞춘다.
        MemberMapper.MemberCredentials member = memberMapper.findCredentialsByEmailForUpdate(normalizedEmail).orElse(null);
        boolean withinLimit = password.getBytes(StandardCharsets.UTF_8).length <= 72;
        String hash = member == null ? unknownMemberHash : member.passwordHash();
        if (!withinLimit || !passwordEncoder.matches(withinLimit ? password : "", hash) || member == null) {
            int accountFailures = recordFailure(accountKey, account, now);
            int ipFailures = recordFailure(ipKey, ip, now);
            if (accountFailures >= MAX_FAILURES || ipFailures >= MAX_FAILURES) throw locked();
            throw invalidCredentials();
        }
        rateLimitMapper.delete(accountKey.scope(), accountKey.hash());
        rateLimitMapper.delete(ipKey.scope(), ipKey.hash());
        return member.memberId();
    }

    /** AI작성: 제한 행을 생성한 뒤 행 잠금 상태로 조회한다. */
    private LoginRateLimitMapper.Limit lockLimit(LimitKey key) {
        rateLimitMapper.insertIfAbsent(key.scope(), key.hash());
        return rateLimitMapper.findForUpdate(key.scope(), key.hash());
    }

    /** AI작성: 실패 횟수를 누적하고 다섯 번째 실패 시 3분 잠금을 설정한다. */
    private int recordFailure(LimitKey key, LoginRateLimitMapper.Limit limit, LocalDateTime now) {
        int failures = limit.lockedUntil() == null ? limit.failures() + 1 : 1;
        rateLimitMapper.update(key.scope(), key.hash(), failures,
                failures >= MAX_FAILURES ? now.plusMinutes(LOCK_MINUTES) : null, now);
        return failures;
    }

    /** AI작성: 현재 시각 기준으로 로그인 제한이 살아 있는지 확인한다. */
    private boolean isLocked(LoginRateLimitMapper.Limit limit, LocalDateTime now) {
        return limit.lockedUntil() != null && now.isBefore(limit.lockedUntil());
    }

    /** AI작성: 가입 여부를 노출하지 않는 동일한 로그인 실패 응답을 만든다. */
    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH-LOGIN-INVALID", "이메일 또는 비밀번호를 확인해 주세요.");
    }

    /** AI작성: 계정 또는 IP 로그인 시도 제한 오류를 만든다. */
    private static ApiException locked() {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AUTH-LOGIN-LOCKED", "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.");
    }

    private record LimitKey(String scope, String hash) {}
}
