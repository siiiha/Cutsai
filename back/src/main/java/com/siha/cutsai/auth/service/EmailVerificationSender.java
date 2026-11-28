package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;

/** AI작성: 이메일 인증 메시지를 실제 발송 채널로 전달하는 계약이다. */
public interface EmailVerificationSender {
    /** AI작성: 인증 목적과 코드를 지정한 이메일 주소로 발송한다. */
    void send(String email, Purpose purpose, String code);
}
