package com.siha.cutsai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** AI작성: 회원가입 요청 본문이다. */
public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        /* AI작성: 비밀번호는 8~16자이며 영문·숫자·특수문자를 각각 포함해야 한다. */
        @NotBlank @Size(min = 8, max = 16)
        @Pattern(regexp = "(?s)^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\\p{P}\\p{S}]).*$") String password,
        @NotBlank @Size(min = 2, max = 12) String nickname,
        /* AI작성: 인증 완료된 현재 코드와 가입 요청을 연결한다. */
        @NotBlank @Pattern(regexp = "[0-9]{6}") String emailVerificationCode) {}
