package com.siha.cutsai.auth.dto;

/** AI작성: 회원가입 명세에 따른 토큰과 회원 정보 응답이다. */
public record RegisterResponse(String access_token, String refresh_token, Member member) {
    /** AI작성: 가입 완료 응답에 포함할 회원 정보다. */
    public record Member(String email, String nickname) {}
}
