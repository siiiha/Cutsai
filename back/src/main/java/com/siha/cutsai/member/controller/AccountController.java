package com.siha.cutsai.member.controller;

import com.siha.cutsai.common.dto.ApiResponse;
import com.siha.cutsai.member.dto.MemberResponse;
import com.siha.cutsai.member.service.MemberService;
import com.siha.cutsai.member.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI작성: 로그인한 회원의 계정 설정 API를 제공한다. */
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final MemberService memberService;
    private final AccountService accountService;

    public AccountController(MemberService memberService, AccountService accountService) {
        this.memberService = memberService;
        this.accountService = accountService;
    }

    /** AI작성: 현재 로그인한 회원의 이메일과 닉네임을 조회한다. */
    @GetMapping
    public ApiResponse<AccountResponse> getAccount(@AuthenticationPrincipal Long memberId) {
        MemberResponse member = memberService.getMe(memberId);
        return ApiResponse.ok(new AccountResponse(member.email(), member.nickname()));
    }

    /** AI작성: 현재 회원의 닉네임을 변경하고 변경된 값을 반환한다. */
    @PatchMapping("/nickname")
    public ApiResponse<NicknameResponse> changeNickname(@AuthenticationPrincipal Long memberId,
                                                         @Valid @RequestBody NicknameRequest request) {
        return ApiResponse.ok(new NicknameResponse(accountService.changeNickname(memberId, request.nickname())));
    }

    /** AI작성: 현재 비밀번호를 확인해 새 비밀번호로 변경한다. */
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long memberId,
                                                @Valid @RequestBody PasswordRequest request) {
        accountService.changePassword(memberId, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** AI작성: 현재 비밀번호를 확인해 회원과 관련 데이터를 삭제한다. */
    @DeleteMapping
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal Long memberId,
                                         @Valid @RequestBody WithdrawalRequest request) {
        accountService.withdraw(memberId, request.currentPassword());
        return ResponseEntity.noContent().build();
    }

    /** AI작성: 필수값 누락과 잘못된 요청 형식을 공통 오류로 반환한다. */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> invalidRequest() {
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION-001", "요청 필수값과 형식을 확인해 주세요."));
    }

    /** AI작성: 설정 화면에 표시할 현재 회원 정보다. */
    public record AccountResponse(String email, String nickname) {}

    /** AI작성: 변경할 닉네임 입력이다. */
    public record NicknameRequest(@NotBlank @Size(min = 2, max = 12) String nickname) {}

    /** AI작성: 현재 비밀번호와 새 비밀번호 입력이다. */
    public record PasswordRequest(@NotBlank String currentPassword,
                                  @NotBlank @Size(min = 8, max = 16)
                                  @Pattern(regexp = "(?s)^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\\p{P}\\p{S}]).*$")
                                  String newPassword) {}

    /** AI작성: 탈퇴 확인을 위한 현재 비밀번호 입력이다. */
    public record WithdrawalRequest(@NotBlank String currentPassword) {}

    /** AI작성: 저장된 닉네임을 화면에 반영하기 위한 응답이다. */
    public record NicknameResponse(String nickname) {}
}
