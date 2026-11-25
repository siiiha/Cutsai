package com.siha.cutsai.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.siha.cutsai.auth.service.EmailVerificationService;
import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** AI작성: 이메일 인증 API의 요청 검증과 HTTP 응답을 확인한다. */
class EmailVerificationControllerTest {
    private EmailVerificationService service;
    private MockMvc mvc;

    /** AI작성: 컨트롤러와 입력 검증기를 독립적으로 준비한다. */
    @BeforeEach
    void setUp() {
        service = mock(EmailVerificationService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new EmailVerificationController(service))
                .setValidator(validator).build();
    }

    /** AI작성: 발송 요청이 201을 반환하고 가입 목적으로 서비스에 전달되는지 확인한다. */
    @Test
    void sendsVerification() throws Exception {
        mvc.perform(post("/api/auth/email-verifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
        verify(service).send("test@example.com", Purpose.SIGNUP);
    }

    /** AI작성: 코드 검증 요청이 200을 반환하고 재설정 목적으로 전달되는지 확인한다. */
    @Test
    void verifiesCode() throws Exception {
        mvc.perform(post("/api/auth/email-verifications/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"purpose\":\"PASSWORD_RESET\",\"code\":\"012345\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(service).verify("test@example.com", Purpose.PASSWORD_RESET, "012345");
    }

    /** AI작성: 재발송 요청이 201을 반환하고 서비스에 전달되는지 확인한다. */
    @Test
    void resendsCode() throws Exception {
        mvc.perform(post("/api/auth/email-verifications/resend").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true));
        verify(service).resend("test@example.com", Purpose.SIGNUP);
    }

    /** AI작성: 잘못된 이메일과 인증 목적은 서비스 호출 전에 거부한다. */
    @Test
    void rejectsInvalidEmailAndPurpose() throws Exception {
        mvc.perform(post("/api/auth/email-verifications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        mvc.perform(post("/api/auth/email-verifications/resend").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"purpose\":\"OTHER\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        verifyNoInteractions(service);
    }

    /** AI작성: 6자리 숫자가 아닌 인증 코드를 거부한다. */
    @Test
    void rejectsInvalidCode() throws Exception {
        mvc.perform(post("/api/auth/email-verifications/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"purpose\":\"SIGNUP\",\"code\":\"12345a\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        verifyNoInteractions(service);
    }
}
