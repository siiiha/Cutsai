import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmailVerification from "../components/EmailVerification.jsx";
import { register } from "../api/authApi.js";

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

// AI작성: 닉네임의 길이와 허용된 문자 사용 여부를 확인한다.
function validateNickname(value) {
  return /^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9_]{2,12}$/.test(value.trim())
    ? "" : "닉네임은 한글, 영문, 숫자, 밑줄만 사용하여 2~12자로 입력해 주세요.";
}

// AI작성: 비밀번호의 길이와 필수 문자 포함 여부를 확인한다.
function validatePassword(value) {
  if (value.length < 8 || value.length > 16) return "비밀번호는 8~16자로 입력해 주세요.";
  if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value) || !/[\p{P}\p{S}]/u.test(value)) {
    return "비밀번호에 영문, 숫자, 특수문자를 각각 하나 이상 포함해 주세요.";
  }
  return "";
}

// AI작성: 인증된 이메일과 계정 정보를 입력받아 회원가입을 요청한다.
export default function Signup() {
  const navigate = useNavigate();
  const [verification, setVerification] = useState({ email: "", verified: false, code: "" });
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  // AI작성: 닉네임 입력란을 벗어난 뒤에는 수정 결과를 바로 반영한다.
  const [nicknameTouched, setNicknameTouched] = useState(false);
  const nicknameError = nicknameTouched && nickname ? validateNickname(nickname) : "";
  // AI작성: 입력란을 벗어난 뒤에는 비밀번호 수정 결과를 바로 반영한다.
  const [passwordTouched, setPasswordTouched] = useState(false);
  const passwordError = passwordTouched && password ? validatePassword(password) : "";
  // AI작성: 비밀번호 확인란을 벗어난 뒤 일치 여부를 표시한다.
  const [passwordConfirmTouched, setPasswordConfirmTouched] = useState(false);
  const passwordConfirmError = passwordConfirmTouched && passwordConfirm && password !== passwordConfirm
    ? "비밀번호 확인이 일치하지 않습니다." : "";

  // AI작성: 입력값을 확인하고 서버가 가입을 승인한 경우에만 완료 화면으로 이동한다.
  async function submit(event) {
    event.preventDefault();
    if (submitting) return;
    const trimmedNickname = nickname.trim();
    if (!verification.verified) {
      setError("이메일 인증을 완료해 주세요.");
      return;
    }
    if (validateNickname(nickname)) {
      setNicknameTouched(true);
      setError("");
      return;
    }
    const validationError = validatePassword(password);
    if (validationError) {
      setPasswordTouched(true);
      setError("");
      return;
    }
    if (password !== passwordConfirm) {
      setPasswordConfirmTouched(true);
      setError("");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      const response = await register({
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
        <div className="brand"><span className="mark">✎</span>Cutsai 회원가입</div>
        <form className="form" onSubmit={submit}>
          {/* AI작성: 회원가입 목적의 인증 상태와 인증번호를 가입 요청에 전달한다. */}
          <EmailVerification purpose="SIGNUP" onChange={setVerification} />
          <label className="field">
            닉네임
            <input placeholder="한글, 영문, 숫자, 밑줄만 사용하여 2~12자로 입력" value={nickname} onChange={(event) => setNickname(event.target.value)}
              onBlur={() => setNicknameTouched(true)} aria-invalid={Boolean(nicknameError)} required />
          </label>
          {/* AI작성: 안내문은 이메일 안내와 같은 작은 글자 크기로 표시한다. */}
          {nicknameError && <small role="alert" style={{ color: "#a53437" }}>{nicknameError}</small>}
          <label className="field">
            비밀번호
            <PasswordInput placeholder="영문, 숫자, 특수문자 각각 포함하여 8~16자로 입력"
              value={password} onChange={(event) => setPassword(event.target.value)} onBlur={() => setPasswordTouched(true)}
              aria-invalid={Boolean(passwordError)} autoComplete="new-password" required />
          </label>
          {/* AI작성: 비밀번호 검증과 가입 오류를 주의 색상으로 표시한다. */}
          {passwordError && <small role="alert" style={{ color: "#a53437" }}>{passwordError}</small>}
          <label className="field">
            비밀번호 확인
            <PasswordInput placeholder="비밀번호 다시 입력" value={passwordConfirm}
              onChange={(event) => setPasswordConfirm(event.target.value)} onBlur={() => setPasswordConfirmTouched(true)}
              aria-invalid={Boolean(passwordConfirmError)} autoComplete="new-password" required />
          </label>
          {/* AI작성: 확인 비밀번호가 다르면 입력란 아래에 오류를 표시한다. */}
          {passwordConfirmError && <small role="alert" style={{ color: "#a53437" }}>{passwordConfirmError}</small>}
          {error && <small role="alert" style={{ color: "#a53437" }}>{error}</small>}
          {/* AI작성: 시안의 공통 클릭 전환을 막아 요청 성공 전에는 완료 화면으로 이동하지 않는다. */}
          <button className="primary" type="submit" disabled={!verification.verified || submitting}
            onClick={(event) => event.stopPropagation()}>
            {submitting ? "가입 중" : "가입하기"}
          </button>
        </form>
        <div className="foot">이미 계정이 있나요?　<Link to="/login">로그인</Link></div>
      </div>
    </div>
  );
}
