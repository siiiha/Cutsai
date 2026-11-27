package com.siha.cutsai.member.service;

/** AI작성: 로그인한 회원의 계정 설정과 탈퇴 처리 계약이다. */
public interface AccountService {
    /** AI작성: 회원의 닉네임을 검증하고 변경한다. */
    String changeNickname(long memberId, String nickname);

    /** AI작성: 현재 비밀번호를 확인하고 새 비밀번호로 변경한다. */
    void changePassword(long memberId, String currentPassword, String newPassword);

    /** AI작성: 현재 비밀번호를 확인하고 회원 및 관련 데이터를 정리한다. */
    void withdraw(long memberId, String currentPassword);
}
