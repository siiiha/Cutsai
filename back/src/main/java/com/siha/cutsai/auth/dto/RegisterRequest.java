package com.siha.cutsai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** AI작성: 회원가입 요청 본문이다. */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        /* AI작성: 회원가입 명세에 맞게 비밀번호와 닉네임 길이를 검증한다. */
        @NotBlank @Size(min = 8, max = 16) String password,
        @NotBlank @Size(min = 2, max = 12) String nickname,
        /* AI작성: 인증 완료된 현재 코드와 가입 요청을 연결한다. */
        @NotBlank @Pattern(regexp = "[0-9]{6}") String emailVerificationCode) {}
