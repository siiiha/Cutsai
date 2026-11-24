import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { logout } from "../api/client.js";

const navigation = [
  { to: "/dashboard", icon: "⌂", label: "대시보드" },
  { to: "/episodes", icon: "▤", label: "회차 관리" },
  { to: "/dialogue", icon: "✦", label: "대사 작업" },
  { to: "/sound-effects", icon: "♫", label: "효과음 소재" },
  { to: "/manuscript/preview", icon: "▣", label: "원고 미리보기" },
  { to: "/settings", icon: "⚙", label: "설정" },
];

// AI작성: 보호 화면의 공통 사이드바와 중첩 라우트 콘텐츠를 표시한다.
export default function WorkLayout() {
  const navigate = useNavigate();

  // AI작성: 로그아웃 API를 호출한 뒤 랜딩 페이지로 이동한다.
  async function signOut() {
    // AI작성: 서버의 refresh 토큰을 폐기한 뒤 메모리 토큰을 비우고 랜딩으로 이동한다.
    await logout();
    navigate("/", { replace: true });
  }

  return <><aside className="side"><div className="brand"><span className="mark">✦</span>Cutsai</div><nav>{navigation.map(({ to, icon, label }) => <NavLink key={to} to={to}><i>{icon}</i>{label}</NavLink>)}</nav><button type="button" className="logout" onClick={signOut}>로그아웃</button></aside><Outlet /></>;
}
