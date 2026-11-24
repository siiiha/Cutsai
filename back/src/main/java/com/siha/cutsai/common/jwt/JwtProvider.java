package com.siha.cutsai.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    public static final Duration ACCESS_VALIDITY = Duration.ofMinutes(15);
    public static final Duration REFRESH_VALIDITY = Duration.ofDays(7);

    private final SecretKey signingKey;

    public JwtProvider(@Value("${jwt.secret:${JWT_SECRET:}}") String secret) {
        if (secret.isBlank()) {
            throw new IllegalArgumentException("Set jwt.secret or JWT_SECRET to a Base64-encoded signing key");
        }
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String createAccessToken(long memberId, Instant now) {
        return createToken(memberId, "access", now, now.plus(ACCESS_VALIDITY));
    }

    public String createRefreshToken(long memberId, Instant now, Instant expiresAt) {
        return createToken(memberId, "refresh", now, expiresAt);
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
