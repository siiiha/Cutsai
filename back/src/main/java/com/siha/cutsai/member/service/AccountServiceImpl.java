package com.siha.cutsai.member.service;

import com.siha.cutsai.auth.mapper.EmailVerificationMapper;
import com.siha.cutsai.auth.mapper.RefreshTokenMapper;
import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.mapper.MemberMapper;
import com.siha.cutsai.member.mapper.MemberMapper.AccountCredentials;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AI작성: 로그인한 회원의 계정 설정과 탈퇴를 처리한다. */
@Service
public class AccountServiceImpl implements AccountService {
    private static final Pattern PASSWORD_FORMAT = Pattern.compile("(?s)^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\\p{P}\\p{S}]).*$");
    private final MemberMapper members;
    private final RefreshTokenMapper tokens;
    private final EmailVerificationMapper verifications;
    private final PasswordEncoder encoder;

    public AccountServiceImpl(MemberMapper members, RefreshTokenMapper tokens,
                              EmailVerificationMapper verifications, PasswordEncoder encoder) {
        this.members = members;
        this.tokens = tokens;
        this.verifications = verifications;
        this.encoder = encoder;
    }

    /** AI작성: 닉네임을 검증하고 중복되지 않을 때 현재 회원 정보에 저장한다. */
    @Override
    @Transactional
    public String changeNickname(long memberId, String nickname) {
        if (nickname == null || nickname.isBlank()) throw invalid("닉네임을 입력해 주세요.");
        String normalized = nickname.strip();
        if (normalized.length() < 2 || normalized.length() > 12) throw invalid("닉네임은 2~12자로 입력해 주세요.");
        AccountCredentials member = lockMember(memberId);
        if (member.nickname().equals(normalized)) return normalized;
        if (members.existsByNickname(normalized)) throw duplicateNickname();
        try {
            if (members.updateNickname(memberId, normalized, now()) != 1) throw memberNotFound();
        } catch (DuplicateKeyException e) {
            // AI작성: 동시 변경으로 사전 중복 검사를 통과한 경우도 중복 오류로 처리한다.
            throw duplicateNickname();
        }
        return normalized;
    }

    /** AI작성: 현재 비밀번호를 검증한 뒤 새 해시를 저장하고 기존 토큰을 폐기한다. */
    @Override
    @Transactional
    public void changePassword(long memberId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 16
                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72
                || !PASSWORD_FORMAT.matcher(newPassword).matches()) {
            throw invalid("새 비밀번호 형식을 확인해 주세요.");
        }
        AccountCredentials member = lockMember(memberId);
        requireCurrentPassword(currentPassword, member.passwordHash());
        LocalDateTime changedAt = now();
        if (members.updatePassword(memberId, encoder.encode(newPassword), changedAt) != 1) throw memberNotFound();
        tokens.revokeAllForMember(memberId, changedAt);
    }

    /** AI작성: 비밀번호 확인 후 인증 기록·회차·토큰·회원을 한 트랜잭션에서 삭제한다. */
    @Override
    @Transactional
    public void withdraw(long memberId, String currentPassword) {
        AccountCredentials member = lockMember(memberId);
        requireCurrentPassword(currentPassword, member.passwordHash());
        verifications.deleteByEmail(member.email());
        members.deleteEpisodeSchedules(memberId);
        tokens.deleteByMemberId(memberId);
        if (members.deleteMember(memberId) != 1) throw memberNotFound();
    }

    /** AI작성: 계정 변경이 겹치지 않도록 현재 회원을 잠금 조회한다. */
    private AccountCredentials lockMember(long memberId) {
        return members.findAccountForUpdate(memberId).orElseThrow(this::memberNotFound);
    }

    /** AI작성: 현재 비밀번호가 실제 저장된 해시와 일치하는지 확인한다. */
    private void requireCurrentPassword(String currentPassword, String passwordHash) {
        if (currentPassword == null || currentPassword.isBlank()
                || currentPassword.getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(currentPassword, passwordHash)) {
            throw invalid("현재 비밀번호를 확인해 주세요.");
        }
    }

    /** AI작성: 데이터베이스에 기록할 현재 UTC 시각이다. */
    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** AI작성: 요청값이나 현재 비밀번호 오류를 공통 오류로 반환한다. */
    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION-001", message);
    }

    /** AI작성: 명세에 정의된 닉네임 중복 오류를 반환한다. */
    private ApiException duplicateNickname() {
        return new ApiException(HttpStatus.CONFLICT, "USER-003", "이미 사용 중인 닉네임입니다.");
    }

    /** AI작성: 로그인 회원을 찾지 못한 경우 기존 회원 조회 오류를 반환한다. */
    private ApiException memberNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다.");
    }
}
