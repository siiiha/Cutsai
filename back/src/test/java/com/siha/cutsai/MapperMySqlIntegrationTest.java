package com.siha.cutsai;

import static org.junit.jupiter.api.Assertions.*;

import com.siha.cutsai.auth.AuthHash;
import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.LoginRateLimitMapper;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.service.AuthService;
import com.siha.cutsai.member.mapper.MemberMapper;
import com.siha.cutsai.member.service.AccountService;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/** AI작성: 명시적으로 활성화한 실제 MySQL에서 XML 매퍼를 확인하고 변경 사항을 되돌린다. */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "CUTSAI_MYSQL_INTEGRATION", matches = "true")
class MapperMySqlIntegrationTest {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MemberMapper members;
    @Autowired private EmailVerificationMapper verifications;
    @Autowired private LoginRateLimitMapper limits;
    @Autowired private RefreshTokenMapper tokens;
    @Autowired private AccountService accounts;
    @Autowired private AuthService authService;
    @Autowired private PasswordEncoder encoder;

    /** AI작성: 테스트 실행 중에만 사용할 임시 토큰 서명 키를 만든다. */
    @DynamicPropertySource
    static void testSigningKey(DynamicPropertyRegistry properties) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        properties.add("jwt.secret", () -> Base64.getEncoder().encodeToString(key));
    }

    /** AI작성: 승인된 cutsai 데이터베이스에서만 테스트 데이터를 생성한다. */
    @BeforeEach
    void checkDatabase() {
        assertEquals("cutsai", jdbc.queryForObject("SELECT DATABASE()", String.class),
                "통합 테스트 대상이 승인된 cutsai 데이터베이스가 아닙니다.");
    }

    /** AI작성: 회원·인증·로그인 제한·토큰 XML 쿼리와 조회 결과 매핑을 실제 MySQL에서 확인한다. */
    @Test
    void exercisesMemberAndAuthenticationMappers() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String email = "mapper-test-" + suffix + "@example.invalid";
        String nickname = "t" + suffix;
        String newNickname = "n" + suffix;
        String limitHash = AuthHash.sha256("limit-" + suffix);
        String tokenHash = AuthHash.sha256("token-" + suffix);
        String nextTokenHash = AuthHash.sha256("next-token-" + suffix);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        assertEquals(1, members.insert(email, "test-password-hash", nickname, now));
        var credentials = members.findCredentialsByEmail(email).orElseThrow();
        long memberId = credentials.memberId();
        assertEquals("test-password-hash", credentials.passwordHash());
        assertEquals(memberId, members.findCredentialsByEmailForUpdate(email).orElseThrow().memberId());
        assertEquals(memberId, members.lockById(memberId).orElseThrow());
        assertTrue(members.existsByEmail(email));
        assertTrue(members.existsByNickname(nickname));
        assertEquals(email, members.findById(memberId).orElseThrow().email());
        assertEquals(nickname, members.findAccountForUpdate(memberId).orElseThrow().nickname());
        assertEquals(1, members.updateNickname(memberId, newNickname, now));
        assertEquals(newNickname, members.findById(memberId).orElseThrow().nickname());
        assertEquals(1, members.updatePassword(memberId, "new-test-hash", now));
        assertEquals("new-test-hash", members.findAccountForUpdate(memberId).orElseThrow().passwordHash());

        verifications.insertIfAbsent(email, "SIGNUP", "code-hash", now.plusMinutes(5), now);
        var verification = verifications.findForUpdate(email, "SIGNUP").orElseThrow();
        assertEquals("code-hash", verification.codeHash());
        assertEquals(0, verification.failedAttempts());
        assertEquals(1, verifications.updateFailedAttempts(email, "SIGNUP", 2));
        assertEquals(2, verifications.findForUpdate(email, "SIGNUP").orElseThrow().failedAttempts());
        assertEquals(1, verifications.markVerified(email, "SIGNUP", now));
        assertNotNull(verifications.findForUpdate(email, "SIGNUP").orElseThrow().verifiedAt());
        assertEquals(1, verifications.consume(email, "SIGNUP", now));
        assertNull(verifications.findForUpdate(email, "SIGNUP").orElseThrow().verifiedAt());
        assertEquals(1, verifications.updateCode(email, "SIGNUP", "new-code-hash", now.plusMinutes(5), now, 1));
        assertEquals("new-code-hash", verifications.findForUpdate(email, "SIGNUP").orElseThrow().codeHash());
        assertEquals(1, verifications.deleteByEmail(email));

        limits.insertIfAbsent("ACCOUNT", limitHash);
        assertEquals(0, limits.findForUpdate("ACCOUNT", limitHash).failures());
        assertEquals(1, limits.update("ACCOUNT", limitHash, 2, now.plusMinutes(3), now));
        assertEquals(2, limits.findForUpdate("ACCOUNT", limitHash).failures());
        assertEquals(1, limits.delete("ACCOUNT", limitHash));

        assertEquals(1, tokens.insert(memberId, tokenHash, now.plusDays(1), now));
        assertNull(tokens.findForUpdate(memberId, tokenHash).orElseThrow().revokedAt());
        assertEquals(1, tokens.revoke(memberId, tokenHash, now));
        assertNotNull(tokens.findForUpdate(memberId, tokenHash).orElseThrow().revokedAt());
        assertEquals(1, tokens.insert(memberId, nextTokenHash, now.plusDays(1), now));
        assertEquals(1, tokens.revokeAllForMember(memberId, now));
        assertNotNull(tokens.findForUpdate(memberId, nextTokenHash).orElseThrow().revokedAt());
        assertEquals(2, tokens.deleteByMemberId(memberId));

        assertEquals(1, jdbc.update("INSERT INTO episode_schedule (member_id, episode_label, stage, due_date) VALUES (?, ?, ?, ?)",
                memberId, "테스트 회차", "콘티", LocalDate.of(2030, 1, 1)));
        assertEquals(1, members.deleteEpisodeSchedules(memberId));
        assertEquals(1, members.deleteMember(memberId));
        assertFalse(members.existsByEmail(email));
    }

    /** AI작성: 실제 탈퇴 후 관련 기록이 정리되고 같은 이메일로 다시 가입할 수 있는지 확인한다. */
    @Test
    void withdrawalRemovesRelatedDataAndAllowsSameEmailSignup() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String email = "withdrawal-test-" + suffix + "@example.invalid";
        String nickname = "w" + suffix;
        String currentPassword = "oldpass1!";
        String newPassword = "newpass1!";
        String code = "123456";
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        assertEquals(1, members.insert(email, encoder.encode(currentPassword), nickname, now));
        long memberId = members.findCredentialsByEmail(email).orElseThrow().memberId();
        assertEquals(1, jdbc.update("INSERT INTO episode_schedule (member_id, episode_label, stage, due_date) VALUES (?, ?, ?, ?)",
                memberId, "테스트 회차", "콘티", LocalDate.of(2030, 1, 1)));
        assertEquals(1, tokens.insert(memberId, AuthHash.sha256("withdraw-token-" + suffix), now.plusDays(1), now));
        verifications.insertIfAbsent(email, "SIGNUP", encoder.encode(code), now.plusMinutes(5), now);
        verifications.insertIfAbsent(email, "PASSWORD_RESET", encoder.encode(code), now.plusMinutes(5), now);

        accounts.withdraw(memberId, currentPassword);

        assertFalse(members.existsByEmail(email));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM episode_schedule WHERE member_id = ?", Integer.class, memberId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM refresh_token WHERE member_id = ?", Integer.class, memberId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM email_verification WHERE email = ?", Integer.class, email));

        verifications.insertIfAbsent(email, "SIGNUP", encoder.encode(code), now.plusMinutes(5), now);
        assertEquals(1, verifications.markVerified(email, "SIGNUP", now));
        var response = authService.register(new RegisterRequest(email, newPassword, nickname, code));
        assertEquals(email, response.member().email());
        assertTrue(members.existsByEmail(email));
    }
}
