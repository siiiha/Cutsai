package com.siha.cutsai.auth.controller;

import com.siha.cutsai.auth.service.PasswordResetService;
import com.siha.cutsai.common.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI작성: 이메일 인증을 이용한 비밀번호 재설정 API를 제공한다. */
@RestController
@RequestMapping("/api/auth/password-resets")
public class PasswordResetController {
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    /** AI작성: 인증번호와 새 비밀번호를 확인해 비밀번호 재설정을 요청한다. */
    @PostMapping
    public ResponseEntity<Void> reset(@Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(request.email(), request.code(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** AI작성: 요청 본문이 없거나 입력 형식이 잘못된 경우 공통 오류를 반환한다. */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION-001", "요청 필수값과 형식을 확인해 주세요."));
    }

    /** AI작성: 비밀번호 재설정에 필요한 이메일, 인증번호, 새 비밀번호다. */
    public record PasswordResetRequest(@NotBlank @Email @Size(max = 254) String email,
                                       @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
                                       @NotBlank @Size(min = 8, max = 16) String newPassword) {}
}
