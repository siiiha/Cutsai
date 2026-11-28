package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI작성: 이메일 인증을 확인해 비밀번호를 변경하고 기존 토큰을 폐기한다. */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final EmailVerificationService verificationService;
    private final MemberMapper memberMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(EmailVerificationService verificationService, MemberMapper memberMapper,
                                    RefreshTokenMapper refreshTokenMapper, PasswordEncoder passwordEncoder) {
        this.verificationService = verificationService;
        this.memberMapper = memberMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /** AI작성: 인증 사용, 비밀번호 변경, 기존 토큰 폐기를 같은 트랜잭션에서 처리한다. */
    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        if (email == null || email.isBlank() || newPassword == null || newPassword.length() < 8
                || newPassword.length() > 16 || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", "요청 필수값과 형식을 확인해 주세요.");
        }
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        verificationService.consumeVerified(normalizedEmail, EmailVerificationService.Purpose.PASSWORD_RESET, code);
        MemberMapper.MemberCredentials member = memberMapper.findCredentialsByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증 정보를 확인해 주세요."));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (memberMapper.updatePassword(member.memberId(), passwordEncoder.encode(newPassword), now) != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AUTH-005", "인증 정보를 확인해 주세요.");
        }
        refreshTokenMapper.revokeAllForMember(member.memberId(), now);
    }
}
