package com.siha.cutsai.auth;

import com.siha.cutsai.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 3;

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String unknownMemberHash = passwordEncoder.encode(UUID.randomUUID().toString());

    public LoginService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Failed credentials must commit their counters before returning the API error.
    @Transactional(noRollbackFor = ApiException.class)
    public long authenticate(String email, String password, String remoteAddress) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        LimitKey accountKey = new LimitKey("ACCOUNT", AuthHash.sha256(normalizedEmail));
        LimitKey ipKey = new LimitKey("IP", AuthHash.sha256(remoteAddress));

        // Always acquire the account row before the IP row.
        Limit account = lockLimit(accountKey);
        Limit ip = lockLimit(ipKey);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (account.isLocked(now) || ip.isLocked(now)) {
            throw locked();
        }

        MemberCredentials member = findMember(normalizedEmail);
        String passwordHash = member == null ? unknownMemberHash : member.passwordHash();
        // BCrypt only uses the first 72 UTF-8 bytes.
        boolean withinBcryptLimit = password.getBytes(StandardCharsets.UTF_8).length <= 72;
        boolean matches = passwordEncoder.matches(withinBcryptLimit ? password : "", passwordHash);
        if (!withinBcryptLimit || !matches || member == null) {
            now = LocalDateTime.now(ZoneOffset.UTC);
            int accountFailures = recordFailure(accountKey, account, now);
            int ipFailures = recordFailure(ipKey, ip, now);
            if (accountFailures >= MAX_FAILURES || ipFailures >= MAX_FAILURES) {
                throw locked();
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH-LOGIN-INVALID",
                    "이메일 또는 비밀번호를 확인해 주세요.");
        }

        reset(accountKey);
        reset(ipKey);
        return member.id();
    }

    private MemberCredentials findMember(String email) {
        return jdbc.query("""
                SELECT member_id, password_hash FROM member
                WHERE email = ? AND BINARY LOWER(email) = ?
                """, (rs, row) -> new MemberCredentials(rs.getLong("member_id"), rs.getString("password_hash")),
                email, email).stream().findFirst().orElse(null);
    }

    private Limit lockLimit(LimitKey key) {
        jdbc.update("""
                INSERT INTO login_rate_limit (limit_scope, subject_hash, updated_at)
                VALUES (?, ?, UTC_TIMESTAMP()) ON DUPLICATE KEY UPDATE failure_count = failure_count
                """, key.scope(), key.hash());
        return jdbc.queryForObject("""
                SELECT failure_count, locked_until FROM login_rate_limit
                WHERE limit_scope = ? AND subject_hash = ? FOR UPDATE
                """, (rs, row) -> new Limit(rs.getInt("failure_count"),
                rs.getObject("locked_until", LocalDateTime.class)), key.scope(), key.hash());
    }

    private int recordFailure(LimitKey key, Limit limit, LocalDateTime now) {
        int failures = limit.lockedUntil() == null ? limit.failures() + 1 : 1;
        LocalDateTime lockedUntil = failures >= MAX_FAILURES ? now.plusMinutes(LOCK_MINUTES) : null;
        jdbc.update("""
                UPDATE login_rate_limit SET failure_count = ?, locked_until = ?, updated_at = ?
                WHERE limit_scope = ? AND subject_hash = ?
                """, failures, lockedUntil, now, key.scope(), key.hash());
        return failures;
    }

    private void reset(LimitKey key) {
        jdbc.update("DELETE FROM login_rate_limit WHERE limit_scope = ? AND subject_hash = ?",
                key.scope(), key.hash());
    }

    private static ApiException locked() {
        return new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AUTH-LOGIN-LOCKED",
                "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.");
    }

    private record MemberCredentials(long id, String passwordHash) {}

    private record LimitKey(String scope, String hash) {}

    private record Limit(int failures, LocalDateTime lockedUntil) {
        boolean isLocked(LocalDateTime now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }
    }
}
