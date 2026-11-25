package com.siha.cutsai.auth.service;

/** AI작성: 이메일 인증을 이용한 비밀번호 재설정 계약이다. */
public interface PasswordResetService {
    /** AI작성: 인증된 이메일과 코드로 새 비밀번호를 설정한다. */
    void resetPassword(String email, String code, String newPassword);
}
