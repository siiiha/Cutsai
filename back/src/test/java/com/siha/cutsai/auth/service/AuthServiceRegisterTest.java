package com.siha.cutsai.auth.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import com.siha.cutsai.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

/** AI작성: 회원가입 시 이메일 인증 사용 완료와 회원 저장 순서를 확인한다. */
class AuthServiceRegisterTest {
    private MemberMapper members;
    private EmailVerificationService verification;
    private AuthService service;

    /** AI작성: 회원가입에 필요한 의존성을 테스트 대역으로 준비한다. */
    @BeforeEach
    void setUp() {
        members = mock(MemberMapper.class);
        verification = mock(EmailVerificationService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("encoded-password");
        service = new AuthService(mock(LoginService.class), mock(JwtProvider.class), mock(RefreshTokenMapper.class),
                members, encoder, verification);
    }

    /** AI작성: 가입 목적 인증을 사용 완료한 뒤 회원을 저장한다. */
    @Test
    void consumesSignupVerificationBeforeSavingMember() {
        service.register(new RegisterRequest("Writer@Example.com", "password123", "작가", "123456"));

        var order = inOrder(verification, members);
        order.verify(verification).consumeVerified("writer@example.com", Purpose.SIGNUP, "123456");
        order.verify(members).insert(eq("writer@example.com"), eq("encoded-password"), eq("작가"), any());
    }

    /** AI작성: 이메일 인증을 확인하지 못하면 회원을 저장하지 않는다. */
    @Test
    void doesNotSaveMemberWhenVerificationFails() {
        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증이 필요합니다."))
                .when(verification).consumeVerified("writer@example.com", Purpose.SIGNUP, "123456");

        assertThrows(ApiException.class,
                () -> service.register(new RegisterRequest("writer@example.com", "password123", "작가", "123456")));
        verify(members, never()).insert(anyString(), anyString(), anyString(), any());
    }
}
