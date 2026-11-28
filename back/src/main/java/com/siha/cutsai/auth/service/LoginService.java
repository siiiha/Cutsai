package com.siha.cutsai.auth.service;

/** AI작성: 계정·IP 로그인 제한을 적용하는 인증 계약이다. */
public interface LoginService {
    /** AI작성: 로그인 자격 증명을 검증하고 성공한 회원 번호를 반환한다. */
    long authenticate(String email, String password, String remoteAddress);
}