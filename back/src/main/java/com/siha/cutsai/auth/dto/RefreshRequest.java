package com.siha.cutsai.auth.dto;

/** AI작성: 쿠키를 사용할 수 없는 클라이언트의 refresh 토큰 요청 본문이다. */
public record RefreshRequest(String refreshToken) {}
