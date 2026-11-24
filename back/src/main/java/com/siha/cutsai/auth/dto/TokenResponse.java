package com.siha.cutsai.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** AI작성: 로그인 또는 재발급 후 반환하는 인증 토큰 정보다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(String accessToken, String tokenType, long accessTokenExpiresIn,
                            String refreshToken, String refreshTokenExpiresAt) {}
