import { useEffect, useRef, useState } from "react";
import { sendEmailVerification, verifyEmail } from "../api/authApi.js";

// AI작성: 가입과 비밀번호 재설정에서 이메일 인증 입력과 상태를 공유한다.
export default function EmailVerification({ purpose, onChange }) {
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [sent, setSent] = useState(false);
  const [verified, setVerified] = useState(false);
  const [expiresAt, setExpiresAt] = useState(0);
  const [remaining, setRemaining] = useState(0);
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState("");
  // AI작성: 회원가입 안내 문구의 성공과 오류 상태를 구분한다.
  const [messageKind, setMessageKind] = useState("");
  const requestVersion = useRef(0);
  // AI작성: 이메일 형식을 브라우저의 이메일 입력 규칙으로 확인한다.
  const emailInput = useRef(null);

  // AI작성: 표시용 남은 시간을 실제 시각과 비교해 갱신한다.
  useEffect(() => {
    if (!expiresAt) return undefined;
    const update = () => {
      const next = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
      setRemaining(next);
      if (next === 0) {
        setVerified(false);
        setMessageKind("error");
        setMessage("인증 시간이 만료되었습니다. 인증번호를 다시 발송해 주세요.");
        onChange?.({ email: email.trim(), verified: false, code: "" });
      }
    };
    update();
    const timer = window.setInterval(update, 1000);
    return () => window.clearInterval(timer);
  }, [expiresAt, email, onChange]);

  // AI작성: 이메일 변경 시 이전 인증 결과와 진행 중인 응답을 무효화한다.
  function changeEmail(event) {
    const next = event.target.value;
    requestVersion.current += 1;
    setEmail(next);
    setCode("");
    setSent(false);
    setVerified(false);
    setExpiresAt(0);
    setRemaining(0);
    setBusy(false);
    setMessage("");
    setMessageKind("");
    onChange?.({ email: next, verified: false, code: "" });
  }

  // AI작성: 발송 또는 재발송 결과에 따라 인증번호와 표시 시간을 초기화한다.
  async function send() {
    if (!email.trim()) {
      setMessageKind("error");
      setMessage("이메일을 입력해 주세요.");
      return;
    }
    if (emailInput.current?.validity.typeMismatch) {
      setMessageKind("error");
      setMessage("이메일 주소를 올바른 형식으로 입력해 주세요. 예: name@example.com");
      return;
    }
    const version = ++requestVersion.current;
    setBusy(true);
    if (purpose !== "SIGNUP") setMessage("");
    try {
      await sendEmailVerification({ email: email.trim(), purpose }, sent);
      if (requestVersion.current !== version) return;
      setSent(true);
      setVerified(false);
      setCode("");
      setExpiresAt(Date.now() + 300000);
      setMessageKind("success");
      setMessage(purpose === "SIGNUP" ? "인증번호는 5분 동안 유효합니다." : "인증번호를 발송했습니다. 이메일을 확인해 주세요.");
      onChange?.({ email: email.trim(), verified: false, code: "" });
    } catch (error) {
      if (requestVersion.current !== version) return;
      setMessageKind("error");
      // AI작성: 서버의 일반적인 검증 오류는 사용자가 이해하기 쉬운 안내로 바꾼다.
      const serverMessage = error.response?.data?.error?.message;
      setMessage(serverMessage === "요청 필수값과 형식을 확인해 주세요."
        ? "입력한 이메일 주소를 확인해 주세요. 예: name@example.com"
        : serverMessage || "인증번호를 발송하지 못했습니다. 다시 시도해 주세요.");
    } finally {
      if (requestVersion.current === version) setBusy(false);
    }
  }

  // AI작성: 현재 이메일과 인증 목적에 맞는 6자리 코드를 서버에서 확인한다.
  async function verifyCode() {
    if (!/^[0-9]{6}$/.test(code)) {
      setMessageKind("error");
      setMessage("6자리 숫자를 입력해 주세요.");
      return;
    }
    const version = ++requestVersion.current;
    setBusy(true);
    setMessage("");
    try {
      await verifyEmail({ email: email.trim(), purpose, code });
      if (requestVersion.current !== version) return;
      setVerified(true);
      setMessageKind("success");
      setMessage("이메일 인증이 완료되었습니다.");
      onChange?.({ email: email.trim(), verified: true, code });
    } catch (error) {
      if (requestVersion.current !== version) return;
      setVerified(false);
      setMessageKind("error");
      onChange?.({ email: email.trim(), verified: false, code: "" });
      setMessage(error.response?.data?.error?.message || "인증번호를 확인하지 못했습니다. 다시 시도해 주세요.");
    } finally {
      if (requestVersion.current === version) setBusy(false);
    }
  }

  const minutes = Math.floor(remaining / 60);
  const seconds = String(remaining % 60).padStart(2, "0");

  return (
    <>
      <label className="field">
        이메일 아이디
        <input ref={emailInput} type="email" placeholder="writer@example.com" value={email} onChange={changeEmail} autoComplete="email" />
      </label>
      <div className="row">
        <input aria-label="인증번호" inputMode="numeric" autoComplete="one-time-code" maxLength={6}
          placeholder="6자리 인증번호" value={code} disabled={!sent || verified || busy}
          onChange={(event) => setCode(event.target.value.replace(/[^0-9]/g, "").slice(0, 6))} />
        <button type="button" onClick={send} disabled={busy || verified}>
          {busy ? "처리 중" : sent ? "재발송" : "인증번호 발송"}
        </button>
        <button type="button" onClick={verifyCode} disabled={!sent || verified || busy || remaining === 0}>
          인증 확인
        </button>
      </div>
      {/* AI작성: 회원가입에서는 안내와 오류를 같은 위치에 표시하고 발송 중에도 유지한다. */}
      {purpose === "SIGNUP" ? (
        <small role="status" style={{ color: messageKind === "error" ? "#a53437" : "#215840" }}>
          {message}{sent && !verified && remaining > 0 ? ` (남은 시간 ${minutes}:${seconds})` : ""}
        </small>
      ) : (
        <>
          <small>{sent ? verified ? "인증이 완료되었습니다." : `남은 시간 ${minutes}:${seconds}` : "인증번호는 5분 동안 유효합니다."}</small>
          {message && <small role="status">{message}</small>}
        </>
      )}
    </>
  );
}
