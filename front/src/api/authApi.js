import client from "./client.js";

// AI작성: 기존 인증 및 세션 함수를 인증 API에서 제공한다.
export { login, recoverAccount, refreshAccessToken, restoreSession, logout } from "./client.js";

// AI작성: 회원가입을 요청한다.
export function register(data) { return client.post("/auth/register", data); }

// AI작성: 이메일 인증번호를 발송하거나 재발송한다.
export function sendEmailVerification(data, resend) {
  return client.post(resend ? "/auth/email-verifications/resend" : "/auth/email-verifications", data);
}

// AI작성: 이메일 인증번호를 확인한다.
export function verifyEmail(data) { return client.post("/auth/email-verifications/verify", data); }

// AI작성: 이메일 인증을 통해 비밀번호 재설정을 요청한다.
export function resetPassword(data) { return client.post("/auth/password-resets", data); }
