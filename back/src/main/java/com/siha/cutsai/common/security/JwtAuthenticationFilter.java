package com.siha.cutsai.common.security;

import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import com.siha.cutsai.member.mapper.MemberMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

final class JwtAuthenticationFilter extends OncePerRequestFilter {
    static final String AUTH_ERROR = JwtAuthenticationFilter.class.getName() + ".error";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final MemberMapper memberMapper;

    JwtAuthenticationFilter(JwtProvider jwtProvider, MemberMapper memberMapper) {
        this.jwtProvider = jwtProvider;
        this.memberMapper = memberMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            try {
                JwtProvider.AccessClaims claims = jwtProvider.readAccessClaims(authorization.substring(BEARER_PREFIX.length()));
                long memberId = claims.memberId();
                // AI작성: 비활성 회원과 탈퇴 전 버전의 토큰은 복구 후에도 보호 API에서 거부한다.
                MemberMapper.TokenState state = memberMapper.findTokenState(memberId).orElse(null);
                if (state == null || !state.active() || state.tokenVersion() != claims.tokenVersion()) {
                    throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "로그인이 필요합니다.");
                }
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(memberId, null, List.of()));
                SecurityContextHolder.setContext(context);
            } catch (ApiException e) {
                request.setAttribute(AUTH_ERROR, e);
            }
        }
        chain.doFilter(request, response);
    }
}
