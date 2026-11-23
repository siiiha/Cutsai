import Landing from "./screens/01-Landing.jsx";
import Signup from "./screens/02-Signup.jsx";
import SignupComplete from "./screens/03-SignupComplete.jsx";
import Login from "./screens/04-Login.jsx";
import Dashboard from "./screens/05-Dashboard.jsx";
import PasswordReset from "./screens/06-PasswordReset.jsx";
import PasswordChanged from "./screens/07-PasswordChanged.jsx";
import DashboardAgain from "./screens/08-DashboardAgain.jsx";
import Settings from "./screens/09-Settings.jsx";
import Withdrawal from "./screens/10-Withdrawal.jsx";
import Episodes from "./screens/11-Episodes.jsx";
import EpisodeCreate from "./screens/12-EpisodeCreate.jsx";
import EpisodeDelete from "./screens/13-EpisodeDelete.jsx";
import LandingAgain from "./screens/14-LandingAgain.jsx";
import Dialogue from "./screens/15-Dialogue.jsx";
import DialogueConsent from "./screens/16-DialogueConsent.jsx";
import ManuscriptUpload from "./screens/17-ManuscriptUpload.jsx";
import ManuscriptPreview from "./screens/18-ManuscriptPreview.jsx";
import DialogueResults from "./screens/19-DialogueResults.jsx";
import SoundEffects from "./screens/20-SoundEffects.jsx";

export const screens = [
  { id: "Landing", path: "/", label: "랜딩", Component: Landing },
  { id: "Signup", path: "/signup", label: "회원가입", Component: Signup },
  { id: "SignupComplete", path: "/signup/complete", label: "가입 완료", Component: SignupComplete },
  { id: "Login", path: "/login", label: "로그인", Component: Login },
  { id: "Dashboard", path: "/dashboard", label: "대시보드", Component: Dashboard },
  { id: "PasswordReset", path: "/password/reset", label: "비밀번호 재설정", Component: PasswordReset },
  { id: "PasswordChanged", path: "/password/changed", label: "비밀번호 변경 완료", Component: PasswordChanged },
  { id: "DashboardAgain", path: "/dashboard/again", label: "대시보드 재진입", Component: DashboardAgain },
  { id: "Settings", path: "/settings", label: "설정", Component: Settings },
  { id: "Withdrawal", path: "/withdrawal", label: "회원 탈퇴", Component: Withdrawal },
  { id: "Episodes", path: "/episodes", label: "회차 관리", Component: Episodes },
  { id: "EpisodeCreate", path: "/episodes/create", label: "회차 등록", Component: EpisodeCreate },
  { id: "EpisodeDelete", path: "/episodes/delete", label: "회차 삭제", Component: EpisodeDelete },
  { id: "LandingAgain", path: "/landing-again", label: "랜딩 재진입", Component: LandingAgain },
  { id: "Dialogue", path: "/dialogue", label: "대사 점검", Component: Dialogue },
  { id: "DialogueConsent", path: "/dialogue/consent", label: "이용 안내", Component: DialogueConsent },
  { id: "ManuscriptUpload", path: "/manuscript/upload", label: "원고 파일 선택", Component: ManuscriptUpload },
  { id: "ManuscriptPreview", path: "/manuscript/preview", label: "모바일 원고 미리보기", Component: ManuscriptPreview },
  { id: "DialogueResults", path: "/dialogue/results", label: "대사 점검 결과", Component: DialogueResults },
  { id: "SoundEffects", path: "/sound-effects", label: "효과음 소재 검색", Component: SoundEffects },
];
