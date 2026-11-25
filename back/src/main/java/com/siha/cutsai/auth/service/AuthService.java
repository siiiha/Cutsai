package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.AuthHash;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return issueTokens(memberId, now, now.plus(JwtProvider.REFRESH_VALIDITY));
    }

    /** AI작성: 이메일·닉네임 중복을 검사하고 BCrypt 비밀번호 해시로 회원을 가입시킨다. */
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String nickname = request.nickname().strip();
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "비밀번호 형식을 확인해 주세요.");
        }
        if (memberMapper.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-EMAIL-DUPLICATED", "이미 사용 중인 이메일입니다.");
        }
        if (memberMapper.existsByNickname(nickname)) {
            throw new ApiException(HttpStatus.CONFLICT, "AUTH-NICKNAME-DUPLICATED", "이미 사용 중인 닉네임입니다.");
        }
        // AI작성: 회원 저장과 같은 트랜잭션에서 가입 목적의 이메일 인증을 확인하고 사용 완료한다.
        emailVerificationService.consumeVerified(email, EmailVerificationService.Purpose.SIGNUP);
        memberMapper.insert(email, passwordEncoder.encode(request.password()), nickname, LocalDateTime.now(ZoneOffset.UTC));
    }

    /** AI작성: 사용 전 refresh 토큰을 폐기하고 최초 만료 시각을 유지한 새 토큰 쌍을 발급한다. */
    @Transactional
    public Tokens refresh(String refreshToken) {
        long memberId = jwtProvider.readRefreshMember(refreshToken);
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
        String accessToken = jwtProvider.createAccessToken(memberId, now);
        String refreshToken = jwtProvider.createRefreshToken(memberId, now, refreshExpiresAt);
        refreshTokenMapper.insert(memberId, AuthHash.sha256(refreshToken),
                LocalDateTime.ofInstant(refreshExpiresAt, ZoneOffset.UTC), LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        return new Tokens(accessToken, refreshToken, refreshExpiresAt);
    }

    /** AI작성: Controller가 쿠키와 응답 본문을 만들 때 쓰는 발급 결과다. */
    public record Tokens(String accessToken, String refreshToken, Instant refreshExpiresAt) {}
}
