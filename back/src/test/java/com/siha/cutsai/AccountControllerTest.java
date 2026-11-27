package com.siha.cutsai;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.siha.cutsai.common.exception.GlobalExceptionHandler;
import com.siha.cutsai.member.controller.AccountController;
import com.siha.cutsai.member.dto.MemberResponse;
import com.siha.cutsai.member.service.AccountService;
import com.siha.cutsai.member.service.MemberService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/** AI작성: 로그인 회원의 계정 API 요청과 응답을 확인한다. */
class AccountControllerTest {
    private MemberService members;
    private AccountService accounts;
    private MockMvc mvc;

    /** AI작성: 인증된 회원과 계정 서비스 대역을 사용하는 컨트롤러를 준비한다. */
    @BeforeEach
    void setUp() {
        members = mock(MemberService.class);
        accounts = mock(AccountService.class);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(1L, null, List.of()));
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AccountController(members, accounts))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    /** AI작성: 테스트에 사용한 인증 상태를 정리한다. */
    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    /** AI작성: 현재 회원의 이메일과 닉네임을 조회한다. */
    @Test
    void returnsCurrentAccount() throws Exception {
        when(members.getMe(1L)).thenReturn(new MemberResponse(1L, "writer@example.com", "작가", LocalDateTime.now()));
        mvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("writer@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("작가"));
        verify(members).getMe(1L);
    }

    /** AI작성: 닉네임 변경 결과를 반환한다. */
    @Test
    void changesNickname() throws Exception {
        when(accounts.changeNickname(1L, "새작가")).thenReturn("새작가");
        mvc.perform(patch("/api/account/nickname").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"새작가\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.nickname").value("새작가"));
        verify(accounts).changeNickname(1L, "새작가");
    }

    /** AI작성: 현재 비밀번호와 새 비밀번호를 전달하고 204를 반환한다. */
    @Test
    void changesPassword() throws Exception {
        mvc.perform(patch("/api/account/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1!\",\"newPassword\":\"newpass1!\"}"))
                .andExpect(status().isNoContent());
        verify(accounts).changePassword(1L, "oldpass1!", "newpass1!");
    }

    /** AI작성: 탈퇴 요청에서 현재 비밀번호를 전달하고 204를 반환한다. */
    @Test
    void withdrawsAccount() throws Exception {
        mvc.perform(delete("/api/account").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1!\"}"))
                .andExpect(status().isNoContent());
        verify(accounts).withdraw(1L, "oldpass1!");
    }

    /** AI작성: 잘못된 닉네임과 비밀번호 조합은 서비스 호출 전에 거부한다. */
    @Test
    void rejectsInvalidInputs() throws Exception {
        mvc.perform(patch("/api/account/nickname").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"가\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        mvc.perform(patch("/api/account/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass1!\",\"newPassword\":\"password1\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION-001"));
        verifyNoInteractions(accounts);
    }
}
