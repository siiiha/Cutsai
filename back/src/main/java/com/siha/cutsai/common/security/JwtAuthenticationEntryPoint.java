package com.siha.cutsai.common.security;

import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** AI작성: Security 필터 단계의 미인증 요청을 공통 API 실패 봉투로 응답한다. */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** AI작성: 필터가 남긴 토큰 오류 또는 일반 미인증 오류를 401 응답으로 만든다. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiException error = (ApiException) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), error == null
                ? ApiResponse.fail("AUTH_REQUIRED", "로그인이 필요합니다.")
                : ApiResponse.fail(error.getCode(), error.getMessage()));
    }
}
