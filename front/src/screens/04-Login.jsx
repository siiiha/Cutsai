import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { login, recoverAccount } from "../api/client.js";

// AI작성: 로그인 요청을 수행하고 성공 시 원래 요청했던 보호 화면으로 이동한다.
export default function Login() {
  // AI작성: 인증 성공 후에는 사용자가 처음 요청했던 보호 경로로 되돌아간다.
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [recoveryAvailable, setRecoveryAvailable] = useState(false);
  const [recoveryError, setRecoveryError] = useState("");

  // AI작성: 폼 기본 제출을 막고 로그인 성공·실패 상태를 화면에 반영한다.
  async function submit(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login(email, password);
      navigate(location.state?.from?.pathname || "/dashboard", { replace: true });
    } catch (requestError) {
      if (requestError.response?.status === 409
          && requestError.response?.data?.error?.code === "AUTH-ACCOUNT-RECOVERY-AVAILABLE") {
        setRecoveryError("");
        setRecoveryAvailable(true);
        setError("탈퇴 유예 중인 계정입니다. 복구를 진행할 수 있습니다.");
      } else {
        setError(requestError.response?.data?.error?.message || "로그인에 실패했습니다.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  async function confirmRecovery() {
    setSubmitting(true);
    setRecoveryError("");
    try {
      await recoverAccount(email, password);
      navigate(location.state?.from?.pathname || "/dashboard", { replace: true });
    } catch (requestError) {
      setRecoveryError(requestError.response?.data?.error?.message || "계정 복구에 실패했습니다. 다시 시도해 주세요.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <div className="auth"><div className="card"><div className="brand"><span className="mark">✦</span>Cutsai</div><h1>로그인</h1><form className="form" onSubmit={submit}><label className="field">이메일<input type="email" placeholder="writer@example.com" value={email} onChange={(event) => setEmail(event.target.value)} required /></label><label className="field">비밀번호<input type="password" placeholder="비밀번호 입력" value={password} onChange={(event) => setPassword(event.target.value)} required /></label>{error && <p role="alert">{error}</p>}<button className="primary" type="submit" disabled={submitting}>{submitting ? "로그인 중..." : "로그인"}</button></form><div className="foot"><Link to="/signup">회원가입</Link><span>·</span><Link to="/password/reset">비밀번호 찾기</Link></div></div></div>
      {recoveryAvailable && <div className="modalshade">
        <div className="card modal" role="dialog" aria-modal="true" aria-labelledby="recovery-title">
          <h2 id="recovery-title">계정 복구</h2>
          <p>탈퇴 후 30일 이내의 계정입니다. 계정과 회차 일정을 복구하고 로그인할까요?</p>
          {recoveryError && <p role="alert">{recoveryError}</p>}
          <div className="actions">
            <button type="button" onClick={() => setRecoveryAvailable(false)} disabled={submitting}>취소</button>
            <button className="primary" type="button" onClick={confirmRecovery} disabled={submitting}>
              {submitting ? "복구 중..." : "복구하고 로그인"}
            </button>
          </div>
        </div>
      </div>}
    </>
  );
}
