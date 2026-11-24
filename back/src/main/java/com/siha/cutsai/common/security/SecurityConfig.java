package com.siha.cutsai.common.security;

import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider, ObjectMapper objectMapper)
            throws Exception {
        return http
                // Cookie transport requires a custom header and a Strict cookie. Other requests use bearer tokens.
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh",
                                "/api/auth/register", "/api/auth/email-verifications",
                                "/api/auth/email-verifications/verify", "/api/auth/email-verifications/resend",
                                "/api/auth/password-resets", "/api/dialogues/check", "/api/dialogues/split").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/onomatopoeias", "/api/sound-effects").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, exception) -> {
                    ApiException error = (ApiException) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    objectMapper.writeValue(response.getWriter(), error == null
                            ? ApiResponse.fail("AUTH_REQUIRED", "로그인이 필요합니다.")
                            : ApiResponse.fail(error.getCode(), error.getMessage()));
                }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
