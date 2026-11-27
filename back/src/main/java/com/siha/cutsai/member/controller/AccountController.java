package com.siha.cutsai.member.controller;

import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.member.dto.MemberResponse;
import com.siha.cutsai.member.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI작성: 로그인한 회원의 계정 설정 API를 제공한다. */
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final MemberService memberService;

    public AccountController(MemberService memberService) {
        this.memberService = memberService;
    }

    /** AI작성: 현재 로그인한 회원의 이메일과 닉네임을 조회한다. */
    @GetMapping
    public ApiResponse<AccountResponse> getAccount(@AuthenticationPrincipal Long memberId) {
        MemberResponse member = memberService.getMe(memberId);
        return ApiResponse.ok(new AccountResponse(member.email(), member.nickname()));
    }

    /** AI작성: 설정 화면에 표시할 현재 회원 정보다. */
    public record AccountResponse(String email, String nickname) {}
}
