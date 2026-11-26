import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmailVerification from "../components/EmailVerification.jsx";
import client from "../api/client.js";

// AI작성: 인증된 이메일과 계정 정보를 입력받아 회원가입을 요청한다.
export default function Signup() {
  const navigate = useNavigate();
  const [verification, setVerification] = useState({ email: "", verified: false, code: "" });
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  // AI작성: 입력값을 확인하고 서버가 가입을 승인한 경우에만 완료 화면으로 이동한다.
  async function submit(event) {
    event.preventDefault();
    if (submitting) return;
    const trimmedNickname = nickname.trim();
    if (!verification.verified) {
      setError("이메일 인증을 완료해 주세요.");
      return;
    }
    if (trimmedNickname.length < 2 || trimmedNickname.length > 12) {
      setError("닉네임은 2~12자로 입력해 주세요.");
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
      const response = await client.post("/auth/register", {
        email: verification.email,
        emailVerificationCode: verification.code,
        nickname: trimmedNickname,
        password,
      });
      // AI작성: 가입 생성 응답일 때만 완료 화면을 표시한다.
      if (response.status !== 201) {
        setError("가입 완료 여부를 확인하지 못했습니다. 다시 시도해 주세요.");
        return;
      }
      // AI작성: 서버 가입 성공을 완료 화면에 전달한다.
      navigate("/signup/complete", { state: { signupCompleted: true }, replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "회원가입에 실패했습니다. 다시 시도해 주세요.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth">
      <div className="card">
        <div className="brand"><span className="mark">✎</span>Cutsai</div>
        <h1>회원가입</h1>
        <form className="form" onSubmit={submit}>
          {/* AI작성: 회원가입 목적의 인증 상태와 인증번호를 가입 요청에 전달한다. */}
          <EmailVerification purpose="SIGNUP" onChange={setVerification} />
          <label className="field">
            닉네임
            <input placeholder="사용할 닉네임 입력" value={nickname} onChange={(event) => setNickname(event.target.value)} required />
          </label>
          <label className="field">
            비밀번호
            <input type="password" placeholder="8~16자 · 영문, 숫자, 특수문자 각각 포함"
              value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="new-password" required />
          </label>
          <label className="field">
            비밀번호 확인
            <input type="password" placeholder="비밀번호 다시 입력" value={passwordConfirm}
              onChange={(event) => setPasswordConfirm(event.target.value)} autoComplete="new-password" required />
          </label>
          {error && <p role="alert">{error}</p>}
          {/* AI작성: 시안의 공통 클릭 전환을 막아 요청 성공 전에는 완료 화면으로 이동하지 않는다. */}
          <button className="primary" type="submit" disabled={!verification.verified || submitting}
            onClick={(event) => event.stopPropagation()}>
            {submitting ? "가입 중..." : "가입하기"}
          </button>
        </form>
        <div className="foot">이미 계정이 있나요?　<Link to="/login">로그인</Link></div>
      </div>
    </div>
  );
}
