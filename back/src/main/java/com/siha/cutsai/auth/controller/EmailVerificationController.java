package com.siha.cutsai.auth.controller;

import com.siha.cutsai.auth.service.EmailVerificationService;
import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import com.siha.cutsai.common.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI작성: 가입과 비밀번호 재설정에 필요한 이메일 인증 API를 제공한다. */
@RestController
@RequestMapping("/api/auth/email-verifications")
public class EmailVerificationController {
    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    /** AI작성: 인증 목적에 맞는 이메일 인증 코드 발송을 요청한다. */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody SendRequest request) {
        service.send(request.email(), request.purpose());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    /** AI작성: 수신한 6자리 인증 코드를 검증한다. */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyRequest request) {
        service.verify(request.email(), request.purpose(), request.code());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /** AI작성: 이전 코드를 무효화하고 새 인증 코드를 발송한다. */
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resend(@Valid @RequestBody SendRequest request) {
        service.resend(request.email(), request.purpose());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    /** AI작성: 요청 본문이나 입력 형식이 올바르지 않을 때 공통 오류를 반환한다. */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION-001", "요청 필수값과 형식을 확인해 주세요."));
    }

    /** AI작성: 발송과 재발송에 필요한 이메일 및 인증 목적이다. */
    public record SendRequest(@NotBlank @Email @Size(max = 254) String email, @NotNull Purpose purpose) {}

    /** AI작성: 인증 코드 검증에 필요한 이메일과 목적, 6자리 코드다. */
    public record VerifyRequest(@NotBlank @Email @Size(max = 254) String email, @NotNull Purpose purpose,
                                @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {}
}
