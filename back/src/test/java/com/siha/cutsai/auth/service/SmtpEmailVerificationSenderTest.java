package com.siha.cutsai.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.siha.cutsai.auth.service.EmailVerificationService.Purpose;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** AI작성: SMTP 발송기에 전달하는 인증 메일 내용을 확인한다. */
class SmtpEmailVerificationSenderTest {
    /** AI작성: 가입 인증 메일에 발신자와 수신자, 코드와 유효 시간이 포함되는지 확인한다. */
    @Test
    void sendsSignupMail() {
        JavaMailSender mail = mock(JavaMailSender.class);
        SmtpEmailVerificationSender sender = new SmtpEmailVerificationSender(mail, "sender@example.com");

        sender.send("recipient@example.com", Purpose.SIGNUP, "012345");

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(message.capture());
        assertEquals("sender@example.com", message.getValue().getFrom());
        assertArrayEquals(new String[]{"recipient@example.com"}, message.getValue().getTo());
        assertTrue(message.getValue().getText().contains("회원가입"));
        assertTrue(message.getValue().getText().contains("012345"));
        assertTrue(message.getValue().getText().contains("5분"));
    }

    /** AI작성: 비밀번호 재설정 목적이 메일 내용에 표시되는지 확인한다. */
    @Test
    void sendsPasswordResetMail() {
        JavaMailSender mail = mock(JavaMailSender.class);
        SmtpEmailVerificationSender sender = new SmtpEmailVerificationSender(mail, "sender@example.com");

        sender.send("recipient@example.com", Purpose.PASSWORD_RESET, "123456");

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(message.capture());
        assertTrue(message.getValue().getText().contains("비밀번호 재설정"));
        assertTrue(message.getValue().getText().contains("123456"));
    }

    /** AI작성: 발신 주소가 없으면 메일을 발송하지 않는다. */
    @Test
    void rejectsMissingSenderAddress() {
        JavaMailSender mail = mock(JavaMailSender.class);
        SmtpEmailVerificationSender sender = new SmtpEmailVerificationSender(mail, " ");

        assertThrows(IllegalStateException.class, () -> sender.send("recipient@example.com", Purpose.SIGNUP, "123456"));
        verifyNoInteractions(mail);
    }
}
