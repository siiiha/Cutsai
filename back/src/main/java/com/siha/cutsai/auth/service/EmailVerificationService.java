package com.siha.cutsai.auth.service;

/** AI작성: 회원가입과 비밀번호 재설정에서 공통으로 사용하는 이메일 인증 계약이다. */
public interface EmailVerificationService {
    /** AI작성: 이메일과 목적에 맞는 인증 코드를 발송한다. */
    void send(String email, Purpose purpose);

    /** AI작성: 기존 인증 코드를 무효화하고 새 코드를 발송한다. */
    void resend(String email, Purpose purpose);

    /** AI작성: 이메일과 목적에 해당하는 6자리 코드를 검증한다. */
    void verify(String email, Purpose purpose, String code);

    /** AI작성: 인증된 이메일과 코드를 확인하고 가입 또는 재설정 트랜잭션에서 사용 완료한다. */
    void consumeVerified(String email, Purpose purpose, String code);

    /** AI작성: 이메일 인증을 요청한 사용 목적이다. */
    enum Purpose {
        SIGNUP,
        PASSWORD_RESET
    }
}
