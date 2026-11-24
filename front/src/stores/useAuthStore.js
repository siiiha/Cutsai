import { create } from "zustand";

// AI작성: 브라우저 메모리에서만 인증 상태를 관리한다.
export const useAuthStore = create((set) => ({
  accessToken: null,
  member: null,
  initialized: false,
  // AI작성: 토큰과 로그인 회원 정보를 함께 저장한다.
  setAuth: ({ accessToken, member = null }) => set({ accessToken, member }),
  // AI작성: 로그아웃·재발급 실패 시 인증 정보를 초기화한다.
  clearAuth: () => set({ accessToken: null, member: null }),
  // AI작성: 최초 refresh 복원 시도가 끝났음을 표시한다.
  setInitialized: () => set({ initialized: true }),
}));
