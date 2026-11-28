package com.siha.cutsai.member.service;

import com.siha.cutsai.common.exception.ApiException;
import com.siha.cutsai.member.dto.MemberResponse;
import com.siha.cutsai.member.mapper.MemberMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** AI작성: 인증된 회원의 내 정보 조회를 처리한다. */
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;

    public MemberServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    /** AI작성: 회원 번호로 본인 정보를 조회하고 응답 DTO로 변환한다. */
    @Override
    public MemberResponse getMe(long memberId) {
        return memberMapper.findById(memberId).map(MemberResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));
    }
}
