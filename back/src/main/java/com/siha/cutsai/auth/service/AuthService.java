package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.AuthHash;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.dto.RegisterResponse;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/** AI작성: 로그인·회원가입·refresh 토큰 회전을 처리한다. */
@Service
public class AuthService {
    private final LoginService loginService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenMapper refreshTokenMapper;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    // AI작성: 회원가입에서 인증된 이메일을 한 번만 사용한다.
    private final EmailVerificationService emailVerificationService;

    public AuthService(LoginService loginService, JwtProvider jwtProvider, RefreshTokenMapper refreshTokenMapper,
                       MemberMapper memberMapper, PasswordEncoder passwordEncoder,
                       EmailVerificationService emailVerificationService) {
        this.loginService = loginService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenMapper = refreshTokenMapper;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    /** AI작성: 로그인 성공 후 최초 refresh 만료 시각을 7일 뒤로 고정해 토큰을 발급한다. */
    @Transactional(noRollbackFor = ApiException.class)
    public Tokens login(String email, String password, String remoteAddress) {
        long memberId = loginService.authenticate(email, password, remoteAddress);
        // AI작성: 비밀번호가 맞아도 탈퇴 유예 계정은 복구 확인 전까지 토큰을 발급하지 않는다.
        MemberMapper.WithdrawalState withdrawal = memberMapper.findWithdrawalForUpdate(memberId)
                .orElseThrow(JwtProvider::invalidRefresh);
        if (withdrawal.withdrawnAt() != null) {
            if (withdrawal.withdrawnAt().isAfter(LocalDateTime.now(ZoneOffset.UTC).minusDays(30))) {
                throw new ApiException(HttpStatus.CONFLICT, "AUTH-ACCOUNT-RECOVERY-AVAILABLE",
                        "탈퇴 유예 중인 계정입니다. 계정 복구를 확인해 주세요.");
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH-LOGIN-INVALID", "이메일 또는 비밀번호를 확인해 주세요.");
        }
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return issueTokens(memberId, now, now.plus(JwtProvider.REFRESH_VALIDITY));
    }

    /** AI작성: 비밀번호를 다시 확인하고 30일 유예가 남은 계정을 복구한 뒤 새 토큰을 발급한다. */
    @Transactional(noRollbackFor = ApiException.class)
    public Tokens recoverAccount(String email, String password, String remoteAddress) {
        long memberId = loginService.authenticate(email, password, remoteAddress);
        MemberMapper.WithdrawalState withdrawal = memberMapper.findWithdrawalForUpdate(memberId)
                .orElseThrow(JwtProvider::invalidRefresh);
        LocalDateTime cutoff = LocalDateTime.now(ZoneOffset.UTC).minusDays(30);
        if (withdrawal.withdrawnAt() == null || !withdrawal.withdrawnAt().isAfter(cutoff)
                || memberMapper.restoreWithinGrace(memberId, cutoff) != 1) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH-LOGIN-INVALID", "이메일 또는 비밀번호를 확인해 주세요.");
        }
        try {
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            return issueTokens(memberId, now, now.plus(JwtProvider.REFRESH_VALIDITY));
        } catch (ApiException e) {
            // AI작성: 복구 후 토큰 발급에서 오류가 나면 계정 복구도 함께 되돌린다.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw e;
        }
    }

    /** AI작성: 이메일·닉네임 중복을 검사하고 BCrypt 비밀번호 해시로 회원을 가입시킨다. */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String nickname = request.nickname().strip();
        // AI작성: 앞뒤 공백을 제거한 실제 닉네임도 가입 길이 기준을 만족해야 한다.
        if (nickname.length() < 2 || nickname.length() > 12) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "닉네임은 2~12자로 입력해 주세요.");
        }
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "비밀번호 형식을 확인해 주세요.");
        }
        if (memberMapper.existsByEmail(email)) {
            // AI작성: 가입 명세의 중복 오류 코드로 반환한다.
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-003", "이미 사용 중인 이메일입니다.");
        }
        if (memberMapper.existsByNickname(nickname)) {
            // AI작성: 이메일과 동일한 중복 오류 코드를 사용하고 안내 문구로 항목을 구분한다.
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-003", "이미 사용 중인 닉네임입니다.");
        }
        // AI작성: 회원 저장과 같은 트랜잭션에서 가입 목적의 이메일 인증을 확인하고 사용 완료한다.
        try {
            emailVerificationService.consumeVerified(email, EmailVerificationService.Purpose.SIGNUP,
                    request.emailVerificationCode());
        } catch (ApiException e) {
            // AI작성: 가입 명세에 맞게 인증 미완료·불일치·만료를 입력 오류로 안내한다.
            if ("AUTH-005".equals(e.getCode())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "이메일 인증을 확인해 주세요.");
            }
            throw e;
        }
        try {
            memberMapper.insert(email, passwordEncoder.encode(request.password()), nickname, LocalDateTime.now(ZoneOffset.UTC));
        } catch (DuplicateKeyException e) {
            // AI작성: 동시 가입으로 사전 검사를 통과해도 DB 중복 제약을 공통 오류로 안내한다.
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-003", "이미 사용 중인 이메일 또는 닉네임입니다.");
        }
        // AI작성: 저장된 회원의 토큰과 응답을 가입 트랜잭션 안에서 함께 생성한다.
        long memberId = memberMapper.findCredentialsByEmail(email)
                .orElseThrow(() -> new IllegalStateException("가입한 회원을 조회할 수 없습니다.")).memberId();
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Tokens tokens = issueTokens(memberId, now, now.plus(JwtProvider.REFRESH_VALIDITY));
        return new RegisterResponse(tokens.accessToken(), tokens.refreshToken(), new RegisterResponse.Member(email, nickname));
    }

    /** AI작성: 사용 전 refresh 토큰을 폐기하고 최초 만료 시각을 유지한 새 토큰 쌍을 발급한다. */
    @Transactional
    public Tokens refresh(String refreshToken) {
        long memberId = jwtProvider.readRefreshMember(refreshToken);
        // AI작성: 재설정과 같은 순서로 회원을 먼저 잠근 뒤 기존 토큰을 확인한다.
        memberMapper.lockById(memberId).orElseThrow(JwtProvider::invalidRefresh);
        String hash = AuthHash.sha256(refreshToken);
        RefreshTokenMapper.RefreshTokenSession session = refreshTokenMapper.findForUpdate(memberId, hash)
                .orElseThrow(JwtProvider::invalidRefresh);
        Instant now = Instant.now();
        Instant expiresAt = session.expiresAt().toInstant(ZoneOffset.UTC);
        if (session.revokedAt() != null || !expiresAt.isAfter(now)) {
            throw JwtProvider.invalidRefresh();
        }
        revoke(memberId, hash, now);
        return issueTokens(memberId, now.truncatedTo(ChronoUnit.SECONDS), expiresAt);
    }

    /** AI작성: 전달된 refresh 토큰을 폐기해 이후 재발급을 막는다. */
    @Transactional
    public void logout(long memberId, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            revoke(memberId, AuthHash.sha256(refreshToken), Instant.now());
        }
    }

    /** AI작성: refresh 토큰 해시의 미폐기 행을 폐기한다. */
    private void revoke(long memberId, String hash, Instant now) {
        refreshTokenMapper.revoke(memberId, hash, LocalDateTime.ofInstant(now, ZoneOffset.UTC));
    }

    /** AI작성: 액세스 JWT와 DB에는 해시만 남기는 refresh 토큰을 함께 발급한다. */
    private Tokens issueTokens(long memberId, Instant now, Instant refreshExpiresAt) {
        // AI작성: 비활성 계정에는 토큰을 발급하지 않고 현재 회원 버전을 액세스 토큰에 담는다.
        MemberMapper.TokenState state = memberMapper.findTokenState(memberId).orElseThrow(JwtProvider::invalidRefresh);
        if (!state.active()) throw JwtProvider.invalidRefresh();
        String accessToken = jwtProvider.createAccessToken(memberId, now, state.tokenVersion());
        String refreshToken = jwtProvider.createRefreshToken(memberId, now, refreshExpiresAt);
        refreshTokenMapper.insert(memberId, AuthHash.sha256(refreshToken),
                LocalDateTime.ofInstant(refreshExpiresAt, ZoneOffset.UTC), LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        return new Tokens(accessToken, refreshToken, refreshExpiresAt);
    }

    /** AI작성: Controller가 쿠키와 응답 본문을 만들 때 쓰는 발급 결과다. */
    public record Tokens(String accessToken, String refreshToken, Instant refreshExpiresAt) {}
}
