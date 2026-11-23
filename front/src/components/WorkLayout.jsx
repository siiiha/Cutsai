import { NavLink, Outlet } from "react-router-dom";

const navigation = [
  { to: "/dashboard", icon: "▤", label: "대시보드" },
  { to: "/episodes", icon: "▱", label: "회차 관리" },
  { to: "/dialogue", icon: "◇", label: "대사 점검" },
  { to: "/sound-effects", icon: "✦", label: "효과음 소재" },
  { to: "/manuscript/preview", icon: "▧", label: "원고 미리보기" },
  { to: "/settings", icon: "⚙", label: "설정" },
];

export default function WorkLayout() {
  return (
    <>
      <aside className="side">
        <div className="brand">
          <span className="mark">✎</span>
          Cutsai
        </div>
        <nav>
          {navigation.map(({ to, icon, label }) => (
            <NavLink key={to} to={to}>
              <i>{icon}</i>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <Outlet />
    </>
  );
}
