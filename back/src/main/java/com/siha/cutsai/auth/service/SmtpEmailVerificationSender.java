package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** AI작성: SMTP를 통해 이메일 인증 코드를 발송한다. */
@Component
@ConditionalOnProperty(name = "spring.mail.host")
public class SmtpEmailVerificationSender implements EmailVerificationSender {
    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailVerificationSender(JavaMailSender mailSender,
                                       @Value("${cutsai.mail.from:${spring.mail.username:}}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** AI작성: 인증 목적과 유효 시간을 포함해 인증 코드를 보낸다. */
    @Override
    public void send(String email, Purpose purpose, String code) {
        if (from.isBlank()) {
            throw new IllegalStateException("메일 발신 주소가 설정되지 않았습니다.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("[컷사이] 이메일 인증번호 안내");
        String action = purpose == Purpose.SIGNUP ? "회원가입" : "비밀번호 재설정";
        message.setText(action + " 인증번호는 " + code + "입니다. 인증번호는 5분 동안 유효합니다.");
        mailSender.send(message);
    }
}
