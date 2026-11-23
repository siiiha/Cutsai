import { useEffect } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes, useNavigate, useLocation } from "react-router-dom";
import WorkLayout from "./components/WorkLayout.jsx";
import NotFound from "./pages/NotFound.jsx";
import { screens } from "./screens.js";
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

function ScreenApp({ screen }) {
  const navigate = useNavigate();
  const { Component, label } = screen;
  useEffect(() => { document.title = "Cutsai · " + label; }, [label]);

  function go(next) {
    const target = screens[next - 1];
    if (!target) return;
    navigate(target.path);
    window.scrollTo(0, 0);
  }

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

function HomeRoute() {
  const { search } = useLocation();
  const legacyPage = new URLSearchParams(search).get("page");
  if (legacyPage === null) return <ScreenApp screen={screens[0]} />;

  const page = Number(legacyPage);
  const target = Number.isInteger(page) && page >= 1 && page <= screens.length ? screens[page - 1] : screens[0];
  return <Navigate to={target.path} replace />;
}

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<HomeRoute />} />
      <Route element={<WorkLayout />}>
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
