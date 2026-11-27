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

    /** AI작성: 기존 호출과 호환되는 버전 0 액세스 토큰을 만든다. */
    public String createAccessToken(long memberId, Instant now) {
        return createAccessToken(memberId, now, 0);
    }

    /** AI작성: 현재 회원 토큰 버전을 포함한 액세스 토큰을 만든다. */
    public String createAccessToken(long memberId, Instant now, long tokenVersion) {
        return createToken(memberId, "access", now, now.plus(ACCESS_VALIDITY), tokenVersion);
    }

    /** AI작성: 기존 만료 시각을 따르는 리프레시 토큰을 만든다. */
    public String createRefreshToken(long memberId, Instant now, Instant expiresAt) {
        return createToken(memberId, "refresh", now, expiresAt, null);
    }

    /** AI작성: 기존 호출에서 사용할 액세스 토큰의 회원 번호를 읽는다. */
    public long readAccessMember(String token) {
        return readAccessClaims(token).memberId();
    }

    /** AI작성: 서명된 액세스 토큰의 회원 번호와 버전을 읽고 이전 토큰은 버전 0으로 처리한다. */
    public AccessClaims readAccessClaims(String token) {
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            if (!"access".equals(claims.get("type")) || claims.getExpiration() == null) {
                throw new JwtException("Unexpected token claims");
            }
            Object version = claims.get("tokenVersion");
            if (version != null && (!(version instanceof Number) || ((Number) version).longValue() < 0)) {
                throw new JwtException("Invalid token version");
            }
            return new AccessClaims(Long.parseLong(claims.getSubject()), version == null ? 0 : ((Number) version).longValue());
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

    /** AI작성: 토큰 종류에 맞는 공통 클레임과 선택적인 액세스 토큰 버전을 서명한다. */
    private String createToken(long memberId, String type, Instant issuedAt, Instant expiresAt, Long tokenVersion) {
        var builder = Jwts.builder()
                .subject(Long.toString(memberId))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt));
        if (tokenVersion != null) builder.claim("tokenVersion", tokenVersion);
        return builder.signWith(signingKey).compact();
    }

    /** AI작성: 액세스 토큰에서 확인한 회원 번호와 버전이다. */
    public record AccessClaims(long memberId, long tokenVersion) {}
}
