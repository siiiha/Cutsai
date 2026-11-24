package com.siha.cutsai.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** AI작성: 로그인 요청 본문이다. */
public record LoginRequest(@NotBlank @Email @Size(max = 254) String email, @NotBlank String password) {}
