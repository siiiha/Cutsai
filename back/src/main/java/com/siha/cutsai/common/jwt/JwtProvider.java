package com.siha.cutsai.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import com.siha.cutsai.common.exception.ApiException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    public static final Duration ACCESS_VALIDITY = Duration.ofMinutes(15);
    public static final Duration REFRESH_VALIDITY = Duration.ofDays(7);

    private final SecretKey signingKey;
    private final JwtParser parser;

    public JwtProvider(@Value("${jwt.secret:${JWT_SECRET:}}") String secret) {
        if (secret.isBlank()) {
            throw new IllegalArgumentException("Set jwt.secret or JWT_SECRET to a Base64-encoded signing key");
        }
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        parser = Jwts.parser().verifyWith(signingKey).build();
    }

    public String createAccessToken(long memberId, Instant now) {
        return createToken(memberId, "access", now, now.plus(ACCESS_VALIDITY));
    }

    public String createRefreshToken(long memberId, Instant now, Instant expiresAt) {
        return createToken(memberId, "refresh", now, expiresAt);
    }

    public long readAccessMember(String token) {
        try {
            return readMember(token, "access");
        } catch (ExpiredJwtException e) {
            if ("access".equals(e.getClaims().get("type"))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "로그인 시간이 만료되었습니다.");
            }
            throw invalidAccess();
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidAccess();
        }
    }

    public long readRefreshMember(String token) {
        try {
            return readMember(token, "refresh");
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidRefresh();
        }
    }

    public static ApiException invalidRefresh() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "AUTH-REFRESH-INVALID",
                "로그인 시간이 만료되었습니다. 다시 로그인해 주세요.");
    }

    private static ApiException invalidAccess() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "로그인 정보가 올바르지 않습니다.");
    }

    private long readMember(String token, String type) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (!type.equals(claims.get("type")) || claims.getExpiration() == null) {
            throw new JwtException("Unexpected token claims");
        }
        return Long.parseLong(claims.getSubject());
    }

    private String createToken(long memberId, String type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(Long.toString(memberId))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }
}
