import Landing from "./pages/LandingPage.jsx";
import Signup from "./pages/SignupPage.jsx";
import SignupComplete from "./pages/SignupCompletePage.jsx";
import Login from "./pages/LoginPage.jsx";
import Dashboard from "./pages/DashboardPage.jsx";
import PasswordReset from "./pages/PasswordResetPage.jsx";
import PasswordChanged from "./pages/PasswordChangedPage.jsx";
import DashboardAgain from "./pages/DashboardAgainPage.jsx";
import Settings from "./pages/SettingsPage.jsx";
import Withdrawal from "./pages/WithdrawalPage.jsx";
import Episodes from "./pages/EpisodesPage.jsx";
import EpisodeCreate from "./pages/EpisodeCreatePage.jsx";
import EpisodeDelete from "./pages/EpisodeDeletePage.jsx";
import LandingAgain from "./pages/LandingAgainPage.jsx";
import Dialogue from "./pages/DialoguePage.jsx";
import DialogueConsent from "./pages/DialogueConsentPage.jsx";
import ManuscriptUpload from "./pages/ManuscriptUploadPage.jsx";
import ManuscriptPreview from "./pages/ManuscriptPreviewPage.jsx";
import DialogueResults from "./pages/DialogueResultsPage.jsx";
import SoundEffects from "./pages/SoundEffectsPage.jsx";

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
