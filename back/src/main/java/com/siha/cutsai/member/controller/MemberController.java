package com.siha.cutsai.member.controller;

import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.member.dto.MemberResponse;
import com.siha.cutsai.member.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI작성: 로그인한 회원의 내 정보 API를 제공한다.
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * AI작성: 토큰에서 얻은 회원 번호로 본인 정보만 반환한다.
     */
    @GetMapping("/me")
    public ApiResponse<MemberResponse> me(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok(memberService.getMe(memberId));
    }
}
