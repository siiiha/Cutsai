package com.siha.cutsai.member.service;

import com.siha.cutsai.member.dto.MemberResponse;

/** AI작성: 인증된 회원의 내 정보 조회 서비스 계약이다. */
public interface MemberService {
    /** AI작성: 회원 번호로 본인 정보를 조회하고 응답 DTO로 변환한다. */
    MemberResponse getMe(long memberId);
}
