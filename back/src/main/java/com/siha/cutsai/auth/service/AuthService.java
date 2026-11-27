package com.siha.cutsai.auth.service;

import com.siha.cutsai.auth.dto.RegisterRequest;
import com.siha.cutsai.auth.dto.RegisterResponse;
import java.time.Instant;

/** AI작성: 로그인·회원가입·토큰 관리와 복구 기능의 서비스 계약이다. */
public interface AuthService {
    /** AI작성: 로그인 성공 시 액세스·리프레시 토큰을 발급한다. */
    Tokens login(String email, String password, String remoteAddress);

    /** AI작성: 탈퇴 후 30일 유예가 남은 계정을 복구하고 새 토큰을 발급한다. */
    Tokens recoverAccount(String email, String password, String remoteAddress);

    /** AI작성: 입력값과 이메일 인증을 검증해 회원을 가입시킨다. */
    RegisterResponse register(RegisterRequest request);

    /** AI작성: 사용된 리프레시 토큰을 회전해 새 토큰 쌍을 발급한다. */
    Tokens refresh(String refreshToken);

    /** AI작성: 현재 회원의 리프레시 토큰을 폐기한다. */
    void logout(long memberId, String refreshToken);

    /** AI작성: 토큰 응답 생성에 필요한 발급 결과다. */
    record Tokens(String accessToken, String refreshToken, Instant refreshExpiresAt) {}
}