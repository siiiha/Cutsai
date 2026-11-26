package com.siha.cutsai.auth.controller;

import com.siha.cutsai.auth.service.AuthService;
import com.siha.cutsai.auth.dto.LoginRequest;
import com.siha.cutsai.auth.dto.RefreshRequest;
import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.dto.RegisterResponse;
import com.siha.cutsai.auth.dto.TokenResponse;
import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.common.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/** AI작성: 회원가입·로그인·재발급·로그아웃 인증 API를 제공한다. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String REFRESH_MODE_HEADER = "X-Refresh-Transport";
    private static final String REFRESH_COOKIE = "refreshToken";
    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, @Value("${jwt.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    /** AI작성: 이메일·비밀번호로 로그인하고 refresh 토큰을 HttpOnly 쿠키로 내려준다. */
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request,
            @RequestHeader(name = REFRESH_MODE_HEADER, required = false) String refreshTransport,
            HttpServletRequest servletRequest) {
        return tokenResponse(authService.login(request.email(), request.password(), servletRequest.getRemoteAddr()),
                usesCookie(refreshTransport));
    }

    /** AI작성: 이메일·비밀번호·닉네임으로 새 회원을 생성한다. */
    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        // AI작성: 가입 응답의 토큰과 회원 정보를 명세의 성공 응답에 담는다.
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(ApiResponse.ok(response));
    }

    /** AI작성: 쿠키 또는 본문의 refresh 토큰을 한 번만 사용해 새 토큰 쌍을 발급한다. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestBody(required = false) RefreshRequest body,
            @RequestHeader(name = REFRESH_MODE_HEADER, required = false) String refreshTransport,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie) {
        boolean useCookie = usesCookie(refreshTransport);
        String refreshToken = useCookie ? refreshCookie : bodyToken(body);
        if (refreshToken == null || refreshToken.isBlank()) throw JwtProvider.invalidRefresh();
        return tokenResponse(authService.refresh(refreshToken), useCookie);
    }

    /** AI작성: 현재 회원의 refresh 토큰을 폐기하고 쿠키를 삭제한다. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long memberId,
            @RequestBody(required = false) RefreshRequest body,
            @RequestHeader(name = REFRESH_MODE_HEADER, required = false) String refreshTransport,
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie) {
        boolean useCookie = usesCookie(refreshTransport);
        authService.logout(memberId, useCookie ? refreshCookie : bodyToken(body));
        ResponseEntity.HeadersBuilder<?> response = ResponseEntity.noContent().cacheControl(CacheControl.noStore());
        if (useCookie) response.header(HttpHeaders.SET_COOKIE, refreshCookie("", 0).toString());
        return response.build();
    }

    /** AI작성: 본문에 토큰이 없는 요청을 안전하게 처리한다. */
    private static String bodyToken(RefreshRequest body) { return body == null ? null : body.refreshToken(); }

    /** AI작성: refresh 토큰 전달 방식이 HttpOnly 쿠키인지 판별한다. */
    private static boolean usesCookie(String refreshTransport) { return "cookie".equals(refreshTransport); }

    /** AI작성: 검증 오류를 공통 실패 봉투로 반환한다. */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION-001", "요청 필수값과 형식을 확인해 주세요."));
    }

    /** AI작성: refresh 쿠키 전송 방식에 맞춰 토큰 응답과 쿠키를 만든다. */
    private ResponseEntity<ApiResponse<TokenResponse>> tokenResponse(AuthService.Tokens tokens, boolean useCookie) {
        ResponseEntity.BodyBuilder response = ResponseEntity.ok().cacheControl(CacheControl.noStore());
        if (useCookie) response.header(HttpHeaders.SET_COOKIE,
                refreshCookie(tokens.refreshToken(), Math.max(0, Duration.between(Instant.now(), tokens.refreshExpiresAt()).toSeconds())).toString());
        return response.body(ApiResponse.ok(new TokenResponse(tokens.accessToken(), "Bearer", JwtProvider.ACCESS_VALIDITY.toSeconds(),
                useCookie ? null : tokens.refreshToken(), tokens.refreshExpiresAt().toString())));
    }

    /** AI작성: refresh 토큰을 JavaScript에서 읽을 수 없는 인증 전용 쿠키로 만든다. */
    private ResponseCookie refreshCookie(String value, long maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, value).path("/api/auth").httpOnly(true).sameSite("Strict")
                .secure(cookieSecure).maxAge(maxAge).build();
    }
}
