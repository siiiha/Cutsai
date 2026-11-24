package com.siha.cutsai.auth;

import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final LoginService loginService;
    private final JwtProvider jwtProvider;
    private final JdbcTemplate jdbc;

    public AuthService(LoginService loginService, JwtProvider jwtProvider, JdbcTemplate jdbc) {
        this.loginService = loginService;
        this.jwtProvider = jwtProvider;
        this.jdbc = jdbc;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public Tokens login(String email, String password, String remoteAddress) {
        long memberId = loginService.authenticate(email, password, remoteAddress);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return issueTokens(memberId, now, now.plus(JwtProvider.REFRESH_VALIDITY));
    }

    private Tokens issueTokens(long memberId, Instant now, Instant refreshExpiresAt) {
        String accessToken = jwtProvider.createAccessToken(memberId, now);
        String refreshToken = jwtProvider.createRefreshToken(memberId, now, refreshExpiresAt);
        jdbc.update("""
                INSERT INTO refresh_token (member_id, token_hash, expires_at, created_at)
                VALUES (?, ?, ?, ?)
                """, memberId, AuthHash.sha256(refreshToken), LocalDateTime.ofInstant(refreshExpiresAt, ZoneOffset.UTC),
                LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        return new Tokens(accessToken, refreshToken, refreshExpiresAt);
    }

    public record Tokens(String accessToken, String refreshToken, Instant refreshExpiresAt) {}
}
