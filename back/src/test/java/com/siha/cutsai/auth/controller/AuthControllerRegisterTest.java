package com.siha.cutsai.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.dto.RegisterResponse;
import com.siha.cutsai.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** AI작성: 회원가입 요청 검증과 성공 응답 형식을 확인한다. */
class AuthControllerRegisterTest {
    private AuthService service;
    private MockMvc mvc;

    /** AI작성: 인증 서비스 대역과 요청 검증기를 사용하는 컨트롤러를 준비한다. */
    @BeforeEach
    void setUp() {
        service = mock(AuthService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(service, false)).setValidator(validator).build();
    }

    /** AI작성: 가입 성공 시 명세에 맞는 토큰과 회원 정보가 반환되는지 확인한다. */
    @Test
    void returnsCreatedWithMemberAndTokens() throws Exception {
        when(service.register(any(RegisterRequest.class))).thenReturn(new RegisterResponse(
                "access-token", "refresh-token", new RegisterResponse.Member("writer@example.com", "작가")));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","nickname":"작가",
                                 "password":"password123!","emailVerificationCode":"123456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.access_token").value("access-token"))
                .andExpect(jsonPath("$.data.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.data.member.email").value("writer@example.com"))
                .andExpect(jsonPath("$.data.member.nickname").value("작가"));
    }

    /** AI작성: 필수 인증번호와 명세상 입력 길이에 맞지 않는 요청을 거부한다. */
    @Test
    void rejectsMissingCodeAndInvalidLengths() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","nickname":"작가","password":"password123"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","nickname":"가",
                                 "password":"short","emailVerificationCode":"123456"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        verifyNoInteractions(service);
    }

    /** AI작성: 영문·숫자·특수문자 중 하나라도 빠진 비밀번호를 거부한다. */
    @Test
    void rejectsPasswordsMissingRequiredCharacterTypes() throws Exception {
        for (String password : new String[]{"password!", "1234567!", "password1"}) {
            String body = """
                    {"email":"writer@example.com","nickname":"작가",
                     "password":"%s","emailVerificationCode":"123456"}
                    """.formatted(password);
            mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        }
        verifyNoInteractions(service);
    }
}
