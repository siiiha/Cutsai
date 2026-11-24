package com.siha.cutsai.member.dto;

import com.siha.cutsai.member.MemberMapper;
import java.time.LocalDateTime;

/** AI작성: 비밀번호 정보를 제외한 내 정보 응답이다. */
public record MemberResponse(long memberId, String email, String nickname, LocalDateTime createdAt) {
    /** AI작성: Mapper 조회 결과를 외부 응답 형태로 변환한다. */
    public static MemberResponse from(MemberMapper.MemberRow member) {
        return new MemberResponse(member.memberId(), member.email(), member.nickname(), member.createdAt());
    }
}
