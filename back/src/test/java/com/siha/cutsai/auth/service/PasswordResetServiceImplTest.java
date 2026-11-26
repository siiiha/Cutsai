package com.siha.cutsai.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** AI작성: 비밀번호 재설정의 인증·저장·토큰 폐기 처리를 확인한다. */
class PasswordResetServiceImplTest {
    private EmailVerificationService verification;
    private MemberMapper members;
    private RefreshTokenMapper tokens;
    private PasswordEncoder encoder;
    private PasswordResetServiceImpl service;

    /** AI작성: 재설정 처리에 필요한 의존성을 테스트 대역으로 준비한다. */
    @BeforeEach
    void setUp() {
        verification = mock(EmailVerificationService.class);
        members = mock(MemberMapper.class);
        tokens = mock(RefreshTokenMapper.class);
        encoder = mock(PasswordEncoder.class);
        service = new PasswordResetServiceImpl(verification, members, tokens, encoder);
    }

    /** AI작성: 인증 후 비밀번호 해시를 저장하고 해당 회원의 토큰을 폐기한다. */
    @Test
    void resetsPasswordAndRevokesTokens() {
        when(members.findCredentialsByEmail("writer@example.com"))
                .thenReturn(Optional.of(new MemberMapper.MemberCredentials(1L, "old-hash")));
        when(encoder.encode("newpass1!")).thenReturn("new-hash");
        when(members.updatePassword(eq(1L), eq("new-hash"), any())).thenReturn(1);

        service.resetPassword("Writer@Example.com", "123456", "newpass1!");

        var order = inOrder(verification, members, tokens);
        order.verify(verification).consumeVerified("writer@example.com", Purpose.PASSWORD_RESET, "123456");
        order.verify(members).findCredentialsByEmail("writer@example.com");
        order.verify(members).updatePassword(eq(1L), eq("new-hash"), any());
        order.verify(tokens).revokeAllForMember(eq(1L), any());
        verify(encoder).encode("newpass1!");
    }

    /** AI작성: 이메일 인증에 실패하면 비밀번호와 토큰을 변경하지 않는다. */
    @Test
    void stopsWhenVerificationFails() {
        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증이 필요합니다."))
                .when(verification).consumeVerified("writer@example.com", Purpose.PASSWORD_RESET, "123456");

        assertThrows(ApiException.class, () -> service.resetPassword("writer@example.com", "123456", "newpass1!"));

        verifyNoInteractions(members, tokens, encoder);
    }

    /** AI작성: 재설정 시 저장하는 BCrypt 해시는 새 비밀번호만 검증한다. */
    @Test
    void storesBcryptHashForNewPassword() {
        BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder(4);
        service = new PasswordResetServiceImpl(verification, members, tokens, realEncoder);
        when(members.findCredentialsByEmail("writer@example.com"))
                .thenReturn(Optional.of(new MemberMapper.MemberCredentials(1L, realEncoder.encode("oldpass1!"))));
        when(members.updatePassword(eq(1L), anyString(), any())).thenReturn(1);

        service.resetPassword("writer@example.com", "123456", "newpass1!");

        ArgumentCaptor<String> storedHash = ArgumentCaptor.forClass(String.class);
        verify(members).updatePassword(eq(1L), storedHash.capture(), any());
        assertNotEquals("newpass1!", storedHash.getValue());
        assertTrue(realEncoder.matches("newpass1!", storedHash.getValue()));
        assertFalse(realEncoder.matches("oldpass1!", storedHash.getValue()));
    }
}
