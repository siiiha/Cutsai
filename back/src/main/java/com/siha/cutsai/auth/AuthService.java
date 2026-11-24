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

    @Transactional
    public Tokens refresh(String refreshToken) {
        long memberId = jwtProvider.readRefreshMember(refreshToken);
        String hash = AuthHash.sha256(refreshToken);
        RefreshSession session = jdbc.query("""
                SELECT expires_at, revoked_at FROM refresh_token
                WHERE token_hash = ? AND member_id = ? FOR UPDATE
                """, (rs, row) -> new RefreshSession(rs.getObject("expires_at", LocalDateTime.class),
                        rs.getObject("revoked_at", LocalDateTime.class)), hash, memberId)
                .stream().findFirst().orElseThrow(JwtProvider::invalidRefresh);

        Instant now = Instant.now();
        Instant expiresAt = session.expiresAt().toInstant(ZoneOffset.UTC);
        if (session.revokedAt() != null || !expiresAt.isAfter(now)) {
            throw JwtProvider.invalidRefresh();
        }

        revoke(memberId, hash, now);
        return issueTokens(memberId, now.truncatedTo(ChronoUnit.SECONDS), expiresAt);
    }

    @Transactional
    public void logout(long memberId, String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            revoke(memberId, AuthHash.sha256(refreshToken), Instant.now());
        }
    }

    private void revoke(long memberId, String hash, Instant now) {
        jdbc.update("""
                UPDATE refresh_token SET revoked_at = ?
                WHERE token_hash = ? AND member_id = ? AND revoked_at IS NULL
                """, LocalDateTime.ofInstant(now, ZoneOffset.UTC), hash, memberId);
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

    private record RefreshSession(LocalDateTime expiresAt, LocalDateTime revokedAt) {}
}
