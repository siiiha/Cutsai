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
                long memberId = jwtProvider.readAccessMember(authorization.substring(BEARER_PREFIX.length()));
                // AI작성: 탈퇴 요청 중이거나 삭제된 회원의 토큰으로 보호 API에 접근하지 못하게 한다.
                if (!memberMapper.isActive(memberId)) {
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
