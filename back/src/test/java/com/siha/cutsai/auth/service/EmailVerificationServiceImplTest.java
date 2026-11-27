package com.siha.cutsai.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.EmailVerificationMapper.Verification;
import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** AI작성: 인증 코드 검증과 재발송 제한의 서비스 규칙을 확인한다. */
class EmailVerificationServiceImplTest {
    private EmailVerificationMapper mapper;
    private MemberMapper members;
    private EmailVerificationServiceImpl service;
    private final Map<String, Verification> states = new HashMap<>();
    private final Map<Purpose, String> delivered = new HashMap<>();
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    /** AI작성: 테스트마다 독립적인 인증 상태와 메일 수신 대역을 준비한다. */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mapper = mock(EmailVerificationMapper.class);
        members = mock(MemberMapper.class);
        ObjectProvider<EmailVerificationSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn((email, purpose, code) -> delivered.put(purpose, code));
        when(mapper.findForUpdate(anyString(), anyString())).thenAnswer(call ->
                Optional.ofNullable(states.get(key(call.getArgument(0), call.getArgument(1)))));
        when(mapper.insertIfAbsent(anyString(), anyString(), anyString(), any(), any())).thenAnswer(call -> {
            String key = key(call.getArgument(0), call.getArgument(1));
            states.putIfAbsent(key, new Verification(call.getArgument(2), call.getArgument(3), null, 0,
                    call.getArgument(4), 0));
            return 1;
        });
        when(mapper.updateCode(anyString(), anyString(), anyString(), any(), any(), anyInt())).thenAnswer(call -> {
            states.put(key(call.getArgument(0), call.getArgument(1)), new Verification(call.getArgument(2),
                    call.getArgument(3), null, 0, call.getArgument(4), call.getArgument(5)));
            return 1;
        });
        when(mapper.updateFailedAttempts(anyString(), anyString(), anyInt())).thenAnswer(call -> {
            String key = key(call.getArgument(0), call.getArgument(1));
            Verification old = states.get(key);
            states.put(key, new Verification(old.codeHash(), old.expiresAt(), old.verifiedAt(),
                    call.getArgument(2), old.resendWindowStartedAt(), old.resendCount()));
            return 1;
        });
        when(mapper.markVerified(anyString(), anyString(), any())).thenAnswer(call -> {
            String key = key(call.getArgument(0), call.getArgument(1));
            Verification old = states.get(key);
            states.put(key, new Verification(old.codeHash(), old.expiresAt(), call.getArgument(2),
                    old.failedAttempts(), old.resendWindowStartedAt(), old.resendCount()));
            return 1;
        });
        when(mapper.consume(anyString(), anyString(), any())).thenAnswer(call -> {
            String key = key(call.getArgument(0), call.getArgument(1));
            Verification old = states.get(key);
            if (old == null || old.verifiedAt() == null) return 0;
            states.put(key, new Verification(old.codeHash(), call.getArgument(2), null, old.failedAttempts(),
                    old.resendWindowStartedAt(), old.resendCount()));
            return 1;
        });
        service = new EmailVerificationServiceImpl(mapper, members, encoder, provider);
    }

    /** AI작성: 발송한 코드를 검증하면 성공 시각을 기록한다. */
    @Test
    void sendsAndVerifiesCode() {
        service.send("TEST@example.com", Purpose.SIGNUP);
        String code = delivered.get(Purpose.SIGNUP);
        assertTrue(code.matches("[0-9]{6}"));
        service.verify("test@example.com", Purpose.SIGNUP, code);
        assertNotNull(state(Purpose.SIGNUP).verifiedAt());
    }

    /** AI작성: 코드 만료 시 올바른 코드도 거부한다. */
    @Test
    void rejectsExpiredCode() {
        service.send("test@example.com", Purpose.SIGNUP);
        Verification old = state(Purpose.SIGNUP);
        states.put(key("test@example.com", "SIGNUP"), new Verification(old.codeHash(), now().minusSeconds(1),
                null, 0, old.resendWindowStartedAt(), old.resendCount()));
        assertEquals("AUTH-005", assertThrows(ApiException.class,
                () -> service.verify("test@example.com", Purpose.SIGNUP, delivered.get(Purpose.SIGNUP))).getCode());
    }

    /** AI작성: 다섯 번 오입력한 뒤에는 올바른 코드도 사용할 수 없다. */
    @Test
    void locksCodeAfterFiveWrongAttempts() {
        service.send("test@example.com", Purpose.SIGNUP);
        String correct = delivered.get(Purpose.SIGNUP);
        String wrong = correct.equals("000000") ? "000001" : "000000";
        for (int i = 0; i < 5; i++) {
            assertThrows(ApiException.class, () -> service.verify("test@example.com", Purpose.SIGNUP, wrong));
        }
        assertEquals(5, state(Purpose.SIGNUP).failedAttempts());
        assertThrows(ApiException.class, () -> service.verify("test@example.com", Purpose.SIGNUP, correct));
    }

    /** AI작성: 새 코드 발급 후 이전 코드를 거부하고 새 코드는 허용한다. */
    @Test
    void resendInvalidatesPreviousCode() {
        service.send("test@example.com", Purpose.SIGNUP);
        String old = delivered.get(Purpose.SIGNUP);
        service.resend("test@example.com", Purpose.SIGNUP);
        String current = delivered.get(Purpose.SIGNUP);
        assertNotEquals(old, current);
        assertThrows(ApiException.class, () -> service.verify("test@example.com", Purpose.SIGNUP, old));
        service.verify("test@example.com", Purpose.SIGNUP, current);
    }

    /** AI작성: 같은 이메일이라도 인증 목적별 상태를 독립적으로 관리한다. */
    @Test
    void separatesPurposes() {
        service.send("test@example.com", Purpose.SIGNUP);
        String signupCode = delivered.get(Purpose.SIGNUP);
        service.send("test@example.com", Purpose.PASSWORD_RESET);
        service.verify("test@example.com", Purpose.SIGNUP, signupCode);
        assertNull(state(Purpose.PASSWORD_RESET).verifiedAt());
        service.verify("test@example.com", Purpose.PASSWORD_RESET, delivered.get(Purpose.PASSWORD_RESET));
        assertNotNull(state(Purpose.PASSWORD_RESET).verifiedAt());
    }

    /** AI작성: 재설정 발송과 재발송은 회원 존재 여부를 조회하지 않는다. */
    @Test
    void passwordResetMailDoesNotCheckWhetherMemberExists() {
        service.send("test@example.com", Purpose.PASSWORD_RESET);
        service.resend("test@example.com", Purpose.PASSWORD_RESET);

        assertNotNull(delivered.get(Purpose.PASSWORD_RESET));
        verifyNoInteractions(members);
    }

    /** AI작성: 최초 발송 경로로 우회해도 재발송 제한이 유지된다. */
    @Test
    void appliesResendLimitToBothPaths() {
        service.send("test@example.com", Purpose.SIGNUP);
        for (int i = 0; i < 5; i++) service.resend("test@example.com", Purpose.SIGNUP);
        assertEquals("AUTH-006", assertThrows(ApiException.class,
                () -> service.send("test@example.com", Purpose.SIGNUP)).getCode());
    }

    /** AI작성: 코드가 만료되어도 아직 끝나지 않은 발송 제한은 유지한다. */
    @Test
    void expiredCodeDoesNotResetResendLimit() {
        service.send("test@example.com", Purpose.SIGNUP);
        for (int i = 0; i < 5; i++) service.resend("test@example.com", Purpose.SIGNUP);
        Verification old = state(Purpose.SIGNUP);
        states.put(key("test@example.com", "SIGNUP"), new Verification(old.codeHash(), now().minusSeconds(1),
                old.verifiedAt(), old.failedAttempts(), old.resendWindowStartedAt(), old.resendCount()));
        assertEquals("AUTH-006", assertThrows(ApiException.class,
                () -> service.resend("test@example.com", Purpose.SIGNUP)).getCode());
    }

    /** AI작성: 발송 제한 구간이 끝나면 이전 코드와 별개로 새 구간을 시작한다. */
    @Test
    void expiredResendWindowAllowsNewCode() {
        service.send("test@example.com", Purpose.SIGNUP);
        for (int i = 0; i < 5; i++) service.resend("test@example.com", Purpose.SIGNUP);
        Verification old = state(Purpose.SIGNUP);
        states.put(key("test@example.com", "SIGNUP"), new Verification(old.codeHash(), old.expiresAt(),
                old.verifiedAt(), old.failedAttempts(), now().minusMinutes(11), old.resendCount()));
        assertDoesNotThrow(() -> service.resend("test@example.com", Purpose.SIGNUP));
        assertEquals(1, state(Purpose.SIGNUP).resendCount());
    }

    /** AI작성: 인증 사용은 한 번만 허용하며 기존 발송 제한을 지우지 않는다. */
    @Test
    void consumingVerificationPreservesResendLimit() {
        service.send("test@example.com", Purpose.SIGNUP);
        for (int i = 0; i < 5; i++) service.resend("test@example.com", Purpose.SIGNUP);
        service.verify("test@example.com", Purpose.SIGNUP, delivered.get(Purpose.SIGNUP));
        service.consumeVerified("test@example.com", Purpose.SIGNUP, delivered.get(Purpose.SIGNUP));
        assertNull(state(Purpose.SIGNUP).verifiedAt());
        assertEquals(5, state(Purpose.SIGNUP).resendCount());
        assertThrows(ApiException.class, () -> service.consumeVerified("test@example.com", Purpose.SIGNUP,
                delivered.get(Purpose.SIGNUP)));
        assertEquals("AUTH-006", assertThrows(ApiException.class,
                () -> service.send("test@example.com", Purpose.SIGNUP)).getCode());
    }

    /** AI작성: 인증에 사용한 번호와 가입 시 제출한 번호가 다르면 인증 사용을 거부한다. */
    @Test
    void rejectsDifferentCodeWhenConsumingVerification() {
        service.send("test@example.com", Purpose.SIGNUP);
        String correct = delivered.get(Purpose.SIGNUP);
        String wrong = correct.equals("000000") ? "000001" : "000000";
        service.verify("test@example.com", Purpose.SIGNUP, correct);

        ApiException error = assertThrows(ApiException.class,
                () -> service.consumeVerified("test@example.com", Purpose.SIGNUP, wrong));
        assertEquals("AUTH-005", error.getCode());
        assertNotNull(state(Purpose.SIGNUP).verifiedAt());
        assertDoesNotThrow(() -> service.consumeVerified("test@example.com", Purpose.SIGNUP, correct));
    }

    /** AI작성: 목적에 해당하는 테스트 인증 상태를 조회한다. */
    private Verification state(Purpose purpose) {
        return states.get(key("test@example.com", purpose.name()));
    }

    /** AI작성: 테스트용 복합 키를 생성한다. */
    private String key(String email, String purpose) {
        return email + ":" + purpose;
    }

    /** AI작성: 테스트에서 사용하는 현재 UTC 시각을 반환한다. */
    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
