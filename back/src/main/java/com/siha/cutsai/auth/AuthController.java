package com.siha.cutsai.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.common.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String REFRESH_MODE_HEADER = "X-Refresh-Transport";

    private final AuthService authService;
    private final boolean cookieSecure;

    public AuthController(AuthService authService, @Value("${jwt.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest login,
            @RequestHeader(name = REFRESH_MODE_HEADER, required = false) String refreshTransport,
            HttpServletRequest request) {
        AuthService.Tokens tokens = authService.login(login.email(), login.password(), request.getRemoteAddr());
        return tokenResponse(tokens, "cookie".equals(refreshTransport));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(
                ApiResponse.fail("VALIDATION-001", "요청의 필수값과 형식을 확인해 주세요."));
    }

    private ResponseEntity<ApiResponse<TokenResponse>> tokenResponse(AuthService.Tokens tokens, boolean useCookie) {
        ResponseEntity.BodyBuilder response = ResponseEntity.ok().cacheControl(CacheControl.noStore());
        if (useCookie) {
            long remainingSeconds = Math.max(0, Duration.between(Instant.now(), tokens.refreshExpiresAt()).toSeconds());
            response.header(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken(), remainingSeconds).toString());
        }
        return response.body(ApiResponse.ok(new TokenResponse(tokens.accessToken(), "Bearer",
                JwtProvider.ACCESS_VALIDITY.toSeconds(), useCookie ? null : tokens.refreshToken(),
                tokens.refreshExpiresAt().toString())));
    }

    private ResponseCookie refreshCookie(String value, long maxAge) {
        return ResponseCookie.from("refreshToken", value)
                .path("/api/auth")
                .httpOnly(true)
                .sameSite("Strict")
                .secure(cookieSecure)
                .maxAge(maxAge)
                .build();
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank String password) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenResponse(String accessToken, String tokenType, long accessTokenExpiresIn,
                                String refreshToken, String refreshTokenExpiresAt) {}
}
