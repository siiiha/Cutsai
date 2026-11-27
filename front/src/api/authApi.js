import client, { refreshAccessToken } from "./client.js";
import { useAuthStore } from "../stores/useAuthStore.js";

const AUTH_ENDPOINTS = Object.freeze({ login: "/auth/login", logout: "/auth/logout", accountRecovery: "/auth/account-recoveries" });
const REFRESH_OPTIONS = { headers: { "X-Refresh-Transport": "cookie" } };

// AI작성: 공통 API 응답 봉투에서 data 값을 꺼낸다.
function tokenData(response) { return response.data?.data; }

// AI작성: 인증 토큰으로 내 정보를 조회한다.
async function loadMember() { return tokenData(await client.get("/members/me")); }

// AI작성: 로그인 성공 후 토큰과 내 정보를 Zustand store에 저장한다.
export async function login(email, password) {
  const tokens = tokenData(await client.post(AUTH_ENDPOINTS.login, { email, password }, REFRESH_OPTIONS));
  useAuthStore.getState().setAuth({ accessToken: tokens.accessToken });
  useAuthStore.getState().setAuth({ accessToken: tokens.accessToken, member: await loadMember() });
  return tokens;
}

// AI작성: 탈퇴 유예 계정을 복구한 뒤 새 인증 토큰과 회원 정보를 저장한다.
export async function recoverAccount(email, password) {
  const tokens = tokenData(await client.post(AUTH_ENDPOINTS.accountRecovery, { email, password }, REFRESH_OPTIONS));
  useAuthStore.getState().setAuth({ accessToken: tokens.accessToken });
  useAuthStore.getState().setAuth({ accessToken: tokens.accessToken, member: await loadMember() });
  return tokens;
}

// AI작성: 앱 최초 실행 시 refresh 복원을 시도하고 완료 상태를 기록한다.
export async function restoreSession() {
  try { await refreshAccessToken(); return true; } catch { return false; } finally { useAuthStore.getState().setInitialized(); }
}

// AI작성: 서버 로그아웃 요청 뒤 store의 인증 정보를 비운다.
export async function logout() {
  try { await client.post(AUTH_ENDPOINTS.logout, undefined, REFRESH_OPTIONS); }
  finally { useAuthStore.getState().clearAuth(); }
}

export { refreshAccessToken };

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
