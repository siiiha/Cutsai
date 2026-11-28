package com.siha.cutsai;

import static org.junit.jupiter.api.Assertions.*;

import com.siha.cutsai.auth.AuthHash;
import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.LoginRateLimitMapper;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.service.AuthService;
import com.siha.cutsai.auth.service.LoginService;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import com.siha.cutsai.member.service.AccountService;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
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
    @Autowired private LoginService loginService;
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

    /** AI작성: 잘못된 현재 비밀번호로는 비밀번호 변경과 탈퇴가 처리되지 않는다. */
    @Test
    void wrongCurrentPasswordPreservesAccountAndRelatedData() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String email = "wrong-password-test-" + suffix + "@example.invalid";
        String nickname = "p" + suffix;
        String currentPassword = "oldpass1!";
        String tokenHash = AuthHash.sha256("wrong-password-token-" + suffix);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        assertEquals(1, members.insert(email, encoder.encode(currentPassword), nickname, now));
        long memberId = members.findCredentialsByEmail(email).orElseThrow().memberId();
        assertEquals(1, jdbc.update("INSERT INTO episode_schedule (member_id, episode_label, stage, due_date) VALUES (?, ?, ?, ?)",
                memberId, "테스트 회차", "콘티", LocalDate.of(2030, 1, 1)));
        assertEquals(1, tokens.insert(memberId, tokenHash, now.plusDays(1), now));
        verifications.insertIfAbsent(email, "SIGNUP", encoder.encode("123456"), now.plusMinutes(5), now);

        assertThrows(ApiException.class, () -> accounts.changePassword(memberId, "wrongpass1!", "newpass1!"));
        assertThrows(ApiException.class, () -> accounts.withdraw(memberId, "wrongpass1!"));

        assertTrue(members.existsByEmail(email));
        assertTrue(encoder.matches(currentPassword, members.findCredentialsByEmail(email).orElseThrow().passwordHash()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM episode_schedule WHERE member_id = ?", Integer.class, memberId));
        assertNull(tokens.findForUpdate(memberId, tokenHash).orElseThrow().revokedAt());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM email_verification WHERE email = ?", Integer.class, email));
    }

    /** AI작성: 계정 변경 후 새 닉네임·비밀번호와 기존 토큰 폐기 상태를 실제 DB에서 확인한다. */
    @Test
    void accountChangesPersistAndRevokeOldRefreshToken() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String email = "account-change-test-" + suffix + "@example.invalid";
        String oldNickname = "a" + suffix;
        String newNickname = "b" + suffix;
        String oldPassword = "oldpass1!";
        String newPassword = "newpass1!";
        String tokenHash = AuthHash.sha256("account-change-token-" + suffix);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);

        assertEquals(1, members.insert(email, encoder.encode(oldPassword), oldNickname, now));
        long memberId = members.findCredentialsByEmail(email).orElseThrow().memberId();
        assertEquals(1, tokens.insert(memberId, tokenHash, now.plusDays(1), now));

        assertEquals(newNickname, accounts.changeNickname(memberId, "  " + newNickname + "  "));
        assertEquals(newNickname, members.findById(memberId).orElseThrow().nickname());
        accounts.changePassword(memberId, oldPassword, newPassword);

        assertNotNull(tokens.findForUpdate(memberId, tokenHash).orElseThrow().revokedAt());
        ApiException error = assertThrows(ApiException.class,
                () -> loginService.authenticate(email, oldPassword, "127.0.0.1"));
        assertEquals("AUTH-LOGIN-INVALID", error.getCode());
        assertEquals(memberId, loginService.authenticate(email, newPassword, "127.0.0.1"));
    }

    /** AI작성: 회원 관련 외래 키가 ERD의 연쇄 삭제 규칙을 따르는지 확인한다. */
    @Test
    void memberForeignKeysUseCascadeDeletion() {
        String sql = """
                SELECT rc.DELETE_RULE
                FROM information_schema.REFERENTIAL_CONSTRAINTS rc
                JOIN information_schema.KEY_COLUMN_USAGE kcu
                  ON rc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
                 AND rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
                 AND rc.TABLE_NAME = kcu.TABLE_NAME
                WHERE rc.CONSTRAINT_SCHEMA = DATABASE()
                  AND kcu.TABLE_NAME = ? AND kcu.COLUMN_NAME = 'member_id'
                  AND kcu.REFERENCED_TABLE_NAME = 'member'
                """;
        // AI작성: 한 테이블이 불일치해도 두 외래 키의 현재 상태를 함께 확인한다.
        List<String> actualRules = List.of("episode_schedule", "refresh_token").stream()
                .map(table -> table + ":" + jdbc.queryForList(sql, String.class, table))
                .toList();
        assertEquals(List.of("episode_schedule:[CASCADE]", "refresh_token:[CASCADE]"), actualRules);
    }

    /** AI작성: 탈퇴 시 버전이 증가하고 유예기간 경계에 맞게 복구되는지 확인한다. */
    @Test
    void withdrawalGraceAndTokenVersionAreStoredInMySql() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String email = "grace-test-" + suffix + "@example.invalid";
        String nickname = "g" + suffix;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime withdrawnAt = now.minusDays(10);

        assertEquals(1, members.insert(email, "test-password-hash", nickname, now));
        long memberId = members.findCredentialsByEmail(email).orElseThrow().memberId();
        assertTrue(members.isActive(memberId));
        assertEquals(0L, members.findTokenState(memberId).orElseThrow().tokenVersion());

        assertEquals(1, members.markWithdrawn(memberId, withdrawnAt));
        assertEquals(0, members.markWithdrawn(memberId, now));
        assertFalse(members.isActive(memberId));
        assertEquals(1L, members.findTokenState(memberId).orElseThrow().tokenVersion());
        assertEquals(withdrawnAt, members.findWithdrawalForUpdate(memberId).orElseThrow().withdrawnAt());

        // AI작성: 기준 시각과 탈퇴 시각이 같으면 30일이 만료된 것으로 처리한다.
        assertEquals(0, members.restoreWithinGrace(memberId, withdrawnAt));
        assertTrue(members.findExpiredWithdrawalsForUpdate(withdrawnAt, 10000).stream()
                .anyMatch(member -> member.memberId() == memberId));
        assertEquals(1, members.restoreWithinGrace(memberId, withdrawnAt.minusSeconds(1)));
        assertTrue(members.isActive(memberId));
        assertEquals(1L, members.findTokenState(memberId).orElseThrow().tokenVersion());

        assertEquals(1, members.markWithdrawn(memberId, now));
        assertEquals(2L, members.findTokenState(memberId).orElseThrow().tokenVersion());
    }
}
