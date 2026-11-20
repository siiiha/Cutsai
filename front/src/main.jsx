import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { screens } from "./screens.js";
import "./styles.css";

const menu = {
  "대시보드": 5, "회차 관리": 11, "대사 점검": 15,
  "효과음 소재": 20, "원고 미리보기": 17, "설정": 9
};

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

function readPage() {
  const raw = new URLSearchParams(window.location.search).get("page");
  const number = Number(raw);
  return Number.isInteger(number) && number >= 1 && number <= screens.length ? number : 1;
}

function ScreenApp() {
  const [page, setPage] = useState(readPage);
  const { Component, label } = screens[page - 1];

  useEffect(() => {
    const sync = () => setPage(readPage());
    window.addEventListener("popstate", sync);
    return () => window.removeEventListener("popstate", sync);
  }, []);

  useEffect(() => { document.title = "작업실 · " + label; }, [label]);

  function go(next) {
    const url = new URL(window.location.href);
    url.searchParams.set("page", String(next));
    window.history.pushState(null, "", url);
    setPage(next);
    window.scrollTo(0, 0);
  }

  function handleClick(event) {
    const button = event.target.closest("button");
    if (button) {
      const next = transitions[page]?.[button.textContent.trim()];
      if (next) go(next);
      return;
    }
    const item = event.target.closest(".side nav > div");
    if (item) {
      const key = item.textContent.trim().replace(/^[▤▱◇✦▧⚙]\s*/, "");
      if (menu[key]) go(menu[key]);
      return;
    }
    const link = event.target.closest("[data-page]");
    if (link) { event.preventDefault(); go(Number(link.dataset.page)); }
  }

  return <div onClick={handleClick}><Component key={page} /></div>;
}

createRoot(document.getElementById("root")).render(<ScreenApp />);
