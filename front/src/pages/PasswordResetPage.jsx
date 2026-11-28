import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmailVerification from "../components/EmailVerification.jsx";
import { resetPassword } from "../api/authApi.js";

// AI작성: 비밀번호를 각 입력란에서 개별적으로 보이거나 숨긴다.
function PasswordInput(props) {
  const [visible, setVisible] = useState(false);
  return (
    <span style={{ position: "relative", display: "block" }}>
      <input {...props} type={visible ? "text" : "password"} style={{ paddingRight: 44 }} />
      <button type="button" aria-label={visible ? "비밀번호 숨기기" : "비밀번호 보기"}
        aria-pressed={visible} onClick={() => setVisible((value) => !value)}
        style={{ position: "absolute", right: 0, top: 0, width: 40, height: "100%", padding: 0, border: 0, background: "transparent", display: "grid", placeItems: "center", cursor: "pointer" }}>
        {/* AI작성: 표시 상태에 따라 열린 눈과 감긴 눈을 구분한다. */}
        <svg aria-hidden="true" width="20" height="20" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
          {visible ? (
            <><path d="M3 10c2.4 3 5.4 4.5 9 4.5s6.6-1.5 9-4.5" /><path d="m5 14-1.5 2M9 15.5l-.5 2M15 15.5l.5 2M19 14l1.5 2" /></>
          ) : (
            <><path d="M2.5 12s3.5-5.5 9.5-5.5 9.5 5.5 9.5 5.5-3.5 5.5-9.5 5.5S2.5 12 2.5 12Z" /><circle cx="12" cy="12" r="2.5" /></>
          )}
        </svg>
      </button>
    </span>
  );
}

// AI작성: 재설정용 이메일 인증과 새 비밀번호 입력을 처리한다.
export default function PasswordReset() {
  const navigate = useNavigate();
  const [verification, setVerification] = useState({ email: "", verified: false, code: "" });
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  // AI작성: 서버가 비밀번호 변경을 완료한 뒤에만 완료 화면으로 이동한다.
  async function submit(event) {
    event.preventDefault();
    if (submitting) return;
    if (!verification.verified) {
      setError("이메일 인증을 완료해 주세요.");
      return;
    }
    if (password.length < 8 || password.length > 16) {
      setError("비밀번호는 8~16자로 입력해 주세요.");
      return;
    }
    // AI작성: 서버와 동일하게 영문·숫자·특수문자를 각각 포함하는지 확인한다.
    if (!/[A-Za-z]/.test(password) || !/[0-9]/.test(password) || !/[\p{P}\p{S}]/u.test(password)) {
      setError("비밀번호에 영문, 숫자, 특수문자를 각각 하나 이상 포함해 주세요.");
      return;
    }
    if (password !== passwordConfirm) {
      setError("비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const response = await resetPassword({
        email: verification.email,
        code: verification.code,
        newPassword: password,
      });
      // AI작성: 서버가 변경 성공을 확정한 경우에만 완료 화면으로 이동한다.
      if (response.status !== 204) {
        setError("비밀번호 변경 완료 여부를 확인하지 못했습니다. 다시 시도해 주세요.");
        return;
      }
      navigate("/password/changed", { state: { passwordResetCompleted: true }, replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "비밀번호를 변경하지 못했습니다. 다시 시도해 주세요.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth">
      <div className="card">
        <div className="brand"><span className="mark">✎</span>Cutsai</div>
        <h1>비밀번호 재설정</h1>
        <form className="form" onSubmit={submit}>
          {/* AI작성: 재설정 목적의 인증된 이메일과 인증번호를 요청에 전달한다. */}
          <EmailVerification purpose="PASSWORD_RESET" onChange={setVerification} />
          <label className="field">
            새 비밀번호
            <PasswordInput placeholder="8~16자 · 영문, 숫자, 특수문자 각각 포함"
              value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" required />
          </label>
          <label className="field">
            새 비밀번호 확인
            <PasswordInput placeholder="비밀번호 다시 입력" value={passwordConfirm}
              onChange={(event) => setPasswordConfirm(event.target.value)} autoComplete="new-password" required />
          </label>
          {error && <p role="alert">{error}</p>}
          {/* AI작성: 시안의 공통 클릭 전환을 막아 서버 성공 전에 완료 화면으로 이동하지 않는다. */}
          <button className="primary" type="submit" disabled={!verification.verified || submitting}
            onClick={(event) => event.stopPropagation()}>
            {submitting ? "변경 중..." : "비밀번호 변경"}
          </button>
        </form>
        <div className="foot"><Link to="/login">로그인으로 돌아가기</Link></div>
      </div>
    </div>
  );
}
