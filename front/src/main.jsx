import { useEffect } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes, useNavigate, useLocation } from "react-router-dom";
import WorkLayout from "./components/WorkLayout.jsx";
import NotFound from "./pages/NotFound.jsx";
import { screens } from "./screens.js";
import { restoreSession } from "./api/client.js";
import { useAuthStore } from "./stores/useAuthStore.js";
import "./styles.css";

const workScreenIds = new Set([
  "Dashboard",
  "DashboardAgain",
  "Settings",
  "Withdrawal",
  "Episodes",
  "EpisodeCreate",
  "EpisodeDelete",
  "Dialogue",
  "DialogueConsent",
  "ManuscriptUpload",
  "ManuscriptPreview",
  "DialogueResults",
  "SoundEffects",
]);

const transitions = {
  1: { "로그인": 4, "회원가입": 2, "시작하기 →": 2 },
  2: { "가입하기": 3 },
  3: { "로그인으로 이동": 4 },
  4: { "로그인": 5 },
  6: { "비밀번호 변경": 7 },
  7: { "로그인으로 이동": 4 },
  9: { "로그아웃": 14, "변경": 6, "회원 탈퇴": 10 },
  10: { "취소": 9 },
  11: { "＋ 회차 등록": 12 },
  12: { "취소": 11, "등록": 11 },
  13: { "취소": 11, "삭제": 11 },
  14: { "로그인": 4, "회원가입": 2, "시작하기 →": 2 },
  15: { "점검하기": 16 },
  16: { "취소": 15, "점검 시작": 19 },
  17: { "모바일로 미리보기 →": 18 },
  18: { "← 파일과 순서 수정": 17 },
  19: { "다시 점검": 15 }
};

// AI작성: 화면 메타데이터를 사용해 공통 클릭 전환과 문서 제목을 적용한다.
function ScreenApp({ screen }) {
  const navigate = useNavigate();
  const { Component, label } = screen;
  useEffect(() => { document.title = "Cutsai · " + label; }, [label]);

  // AI작성: 화면 번호에 해당하는 경로로 이동하고 스크롤을 맨 위로 올린다.
  function go(next) {
    const target = screens[next - 1];
    if (!target) return;
    navigate(target.path);
    window.scrollTo(0, 0);
  }

  // AI작성: 화면의 버튼 텍스트와 전환 맵을 대조해 다음 화면으로 이동한다.
  function handleClick(event) {
    const button = event.target.closest("button");
    if (button) {
      const next = transitions[screens.indexOf(screen) + 1]?.[button.textContent.trim()];
      if (next) go(next);
      return;
    }
  }

  return <div onClick={handleClick}><Component /></div>;
}

// AI작성: 레거시 page 쿼리 파라미터를 현재 라우트 경로로 변환한다.
function HomeRoute() {
  const { search } = useLocation();
  const legacyPage = new URLSearchParams(search).get("page");
  if (legacyPage === null) return <ScreenApp screen={screens[0]} />;

  const page = Number(legacyPage);
  const target = Number.isInteger(page) && page >= 1 && page <= screens.length ? screens[page - 1] : screens[0];
  return <Navigate to={target.path} replace />;
}

// AI작성: 인증된 사용자만 작업 화면을 렌더링하는 라우트 가드다.
function ProtectedRoute() {
  // AI작성: 새로고침 뒤에도 HttpOnly refresh 쿠키로 세션을 복원한 뒤 보호 화면을 렌더링한다.
  const accessToken = useAuthStore((state) => state.accessToken);
  const initialized = useAuthStore((state) => state.initialized);
  const location = useLocation();

  // AI작성: 최초 진입에는 세션을 복원하고, 재발급 실패 이벤트에는 로그인 화면으로 전환한다.
  useEffect(() => {
    if (!initialized) restoreSession();
  }, [initialized]);

  if (!initialized) return <div className="auth">Loading...</div>;
  if (!accessToken) return <Navigate to="/login" replace state={{ from: location }} />;
  return <WorkLayout />;
}

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<HomeRoute />} />
      <Route element={<ProtectedRoute />}>
        {screens.filter((screen) => workScreenIds.has(screen.id)).map((screen) => (
          <Route key={screen.path} path={screen.path} element={<ScreenApp screen={screen} />} />
        ))}
      </Route>
      {screens.filter((screen) => screen.path !== "/" && !workScreenIds.has(screen.id)).map((screen) => (
        <Route key={screen.path} path={screen.path} element={<ScreenApp screen={screen} />} />
      ))}
      <Route path="*" element={<NotFound />} />
    </Routes>
  </BrowserRouter>
);
