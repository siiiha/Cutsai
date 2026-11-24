import axios from "axios";
import { useAuthStore } from "../stores/useAuthStore.js";

const AUTH_ENDPOINTS = Object.freeze({ login: "/auth/login", refresh: "/auth/refresh", logout: "/auth/logout" });
const REFRESH_OPTIONS = { headers: { "X-Refresh-Transport": "cookie" } };
const client = axios.create({ baseURL: "/api", timeout: 10000, withCredentials: true });
let refreshInFlight = null;

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

// AI작성: refresh 쿠키로 access token을 재발급하고 동시에 들어온 요청은 하나의 Promise를 공유한다.
export function refreshAccessToken() {
  if (!refreshInFlight) {
    refreshInFlight = client.post(AUTH_ENDPOINTS.refresh, undefined, REFRESH_OPTIONS).then(async (response) => {
      const tokens = tokenData(response);
      if (!tokens?.accessToken) throw new Error("Refresh response has no access token");
      useAuthStore.getState().setAuth({ accessToken: tokens.accessToken });
      useAuthStore.getState().setAuth({ accessToken: tokens.accessToken, member: await loadMember() });
      return tokens.accessToken;
    }).finally(() => { refreshInFlight = null; });
  }
  return refreshInFlight;
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

client.interceptors.request.use((config) => {
  const accessToken = useAuthStore.getState().accessToken;
  if (accessToken && ![AUTH_ENDPOINTS.login, AUTH_ENDPOINTS.refresh].includes(config.url)) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

client.interceptors.response.use(undefined, async (error) => {
  const request = error.config;
  const isTokenRequest = [AUTH_ENDPOINTS.login, AUTH_ENDPOINTS.refresh].includes(request?.url);
  const code = error.response?.data?.error?.code;
  if (code !== "TOKEN_EXPIRED" || !request || request._retried || isTokenRequest) return Promise.reject(error);
  request._retried = true;
  try { request.headers.Authorization = `Bearer ${await refreshAccessToken()}`; return client(request); }
  catch (refreshError) { useAuthStore.getState().clearAuth(); return Promise.reject(refreshError); }
});

export default client;
