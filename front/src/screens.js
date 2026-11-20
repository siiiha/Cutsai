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
  { id: "Landing", label: "랜딩", Component: Landing },
  { id: "Signup", label: "회원가입", Component: Signup },
  { id: "SignupComplete", label: "가입 완료", Component: SignupComplete },
  { id: "Login", label: "로그인", Component: Login },
  { id: "Dashboard", label: "대시보드", Component: Dashboard },
  { id: "PasswordReset", label: "비밀번호 재설정", Component: PasswordReset },
  { id: "PasswordChanged", label: "비밀번호 변경 완료", Component: PasswordChanged },
  { id: "DashboardAgain", label: "대시보드 재진입", Component: DashboardAgain },
  { id: "Settings", label: "설정", Component: Settings },
  { id: "Withdrawal", label: "회원 탈퇴", Component: Withdrawal },
  { id: "Episodes", label: "회차 관리", Component: Episodes },
  { id: "EpisodeCreate", label: "회차 등록", Component: EpisodeCreate },
  { id: "EpisodeDelete", label: "회차 삭제", Component: EpisodeDelete },
  { id: "LandingAgain", label: "랜딩 재진입", Component: LandingAgain },
  { id: "Dialogue", label: "대사 점검", Component: Dialogue },
  { id: "DialogueConsent", label: "이용 안내", Component: DialogueConsent },
  { id: "ManuscriptUpload", label: "원고 파일 선택", Component: ManuscriptUpload },
  { id: "ManuscriptPreview", label: "모바일 원고 미리보기", Component: ManuscriptPreview },
  { id: "DialogueResults", label: "대사 점검 결과", Component: DialogueResults },
  { id: "SoundEffects", label: "효과음 소재 검색", Component: SoundEffects },
];
