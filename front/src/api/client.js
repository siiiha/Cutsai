import axios from "axios";

const AUTH_ENDPOINTS = Object.freeze({
  login: "/auth/login",
  refresh: "/auth/refresh",
  logout: "/auth/logout",
});
const REFRESH_TRANSPORT_HEADER = "X-Refresh-Transport";
const REFRESH_TRANSPORT = "cookie";

const client = axios.create({
  baseURL: "/api",
  timeout: 10000,
  withCredentials: true,
});

// AI작성: 액세스 토큰은 브라우저 저장소에 남기지 않고 메모리에서만 관리한다.
let accessToken = null;
let refreshInFlight = null;

// AI작성: 메모리에 유효한 액세스 토큰이 있는지 반환한다.
export function hasSession() {
  return Boolean(accessToken);
}

// AI작성: 액세스 토큰을 저장하거나 null로 초기화한다.
export function setAccessToken(token) {
  accessToken = token || null;
}

// AI작성: 공통 API 응답 봉투에서 실제 응답 데이터를 추출한다.
function tokenData(response) {
  return response.data?.data;
}

// AI작성: 이메일과 비밀번호로 로그인하고 발급된 액세스 토큰을 메모리에 저장한다.
export async function login(email, password) {
  const response = await client.post(AUTH_ENDPOINTS.login, { email, password }, {
    headers: { [REFRESH_TRANSPORT_HEADER]: REFRESH_TRANSPORT },
  });
  const tokens = tokenData(response);
  setAccessToken(tokens?.accessToken);
  return tokens;
}

// AI작성: 새로고침 뒤 refresh 쿠키로 세션을 복원한다.
export async function restoreSession() {
  if (accessToken) return true;
  try {
    await refreshAccessToken();
    return true;
  } catch {
    return false;
  }
}

// AI작성: 동시에 발생한 만료 요청은 하나의 refresh 요청을 공유하도록 액세스 토큰을 재발급한다.
async function refreshAccessToken() {
  if (!refreshInFlight) {
    refreshInFlight = client.post(AUTH_ENDPOINTS.refresh, undefined, {
      headers: { [REFRESH_TRANSPORT_HEADER]: REFRESH_TRANSPORT },
    }).then((response) => {
      const tokens = tokenData(response);
      if (!tokens?.accessToken) throw new Error("Refresh response has no access token");
      setAccessToken(tokens.accessToken);
      return tokens.accessToken;
    }).finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

// AI작성: 서버에 로그아웃을 요청하고 성공·실패와 무관하게 로컬 액세스 토큰을 제거한다.
export async function logout() {
  try {
    await client.post(AUTH_ENDPOINTS.logout, undefined, {
      headers: { [REFRESH_TRANSPORT_HEADER]: REFRESH_TRANSPORT },
    });
  } finally {
    setAccessToken(null);
  }
}

// AI작성: 로그인·재발급 요청을 제외한 API 요청에 Bearer 액세스 토큰을 추가한다.
client.interceptors.request.use((config) => {
  if (accessToken && ![AUTH_ENDPOINTS.login, AUTH_ENDPOINTS.refresh].includes(config.url)) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// AI작성: 만료된 액세스 토큰 요청은 재발급에 성공한 경우 한 번만 원래 요청을 재시도한다.
client.interceptors.response.use(undefined, async (error) => {
  const request = error.config;
  const isAuthRequest = [AUTH_ENDPOINTS.login, AUTH_ENDPOINTS.refresh].includes(request?.url);
  if (error.response?.status !== 401 || !request || request._retried || isAuthRequest) {
    return Promise.reject(error);
  }

  request._retried = true;
  try {
    const token = await refreshAccessToken();
    request.headers = request.headers || {};
    request.headers.Authorization = `Bearer ${token}`;
    return client(request);
  } catch (refreshError) {
    setAccessToken(null);
    window.dispatchEvent(new Event("auth:expired"));
    return Promise.reject(refreshError);
  }
});

export default client;
