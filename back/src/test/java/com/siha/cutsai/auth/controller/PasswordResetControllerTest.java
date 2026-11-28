package com.siha.cutsai.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.siha.cutsai.auth.service.PasswordResetService;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** AI작성: 비밀번호 재설정 API의 입력 검증과 성공 응답을 확인한다. */
class PasswordResetControllerTest {
    private PasswordResetService service;
    private MockMvc mvc;

    /** AI작성: 서비스 대역과 입력 검증기를 사용하는 컨트롤러를 준비한다. */
    @BeforeEach
    void setUp() {
        service = mock(PasswordResetService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new PasswordResetController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    /** AI작성: 올바른 요청이 서비스에 전달되고 성공 응답으로 204를 반환한다. */
    @Test
    void acceptsValidPassword() throws Exception {
        mvc.perform(post("/api/auth/password-resets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","code":"123456","newPassword":"password1!"}
                                """))
                .andExpect(status().isNoContent());
        verify(service).resetPassword("writer@example.com", "123456", "password1!");
    }

    /** AI작성: 영문·숫자·특수문자 중 하나라도 없으면 서비스 호출 전에 거부한다. */
    @Test
    void rejectsPasswordsMissingRequiredCharacterTypes() throws Exception {
        for (String password : new String[]{"password!", "1234567!", "password1"}) {
            String body = """
                    {"email":"writer@example.com","code":"123456","newPassword":"%s"}
                    """.formatted(password);
            mvc.perform(post("/api/auth/password-resets").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        }
        verifyNoInteractions(service);
    }

    /** AI작성: 인증되지 않은 재설정 요청은 공통 인증 오류 응답으로 거부한다. */
    @Test
    void rejectsUnverifiedReset() throws Exception {
        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증 정보를 확인해 주세요."))
                .when(service).resetPassword("writer@example.com", "123456", "password1!");

        mvc.perform(post("/api/auth/password-resets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"writer@example.com","code":"123456","newPassword":"password1!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH-005"));
    }
}
