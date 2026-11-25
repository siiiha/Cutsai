import { useEffect, useRef, useState } from "react";
import client from "../api/client.js";

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
  const requestVersion = useRef(0);

  // AI작성: 표시용 남은 시간을 실제 시각과 비교해 갱신한다.
  useEffect(() => {
    if (!expiresAt) return undefined;
    const update = () => setRemaining(Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000)));
    update();
    const timer = window.setInterval(update, 1000);
    return () => window.clearInterval(timer);
  }, [expiresAt]);

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
    onChange?.({ email: next, verified: false, code: "" });
  }

  // AI작성: 발송 또는 재발송 결과에 따라 인증번호와 표시 시간을 초기화한다.
  async function send() {
    if (!email.trim()) {
      setMessage("이메일을 입력해 주세요.");
      return;
    }
    const version = ++requestVersion.current;
    setBusy(true);
    setMessage("");
    try {
      await client.post(sent ? "/auth/email-verifications/resend" : "/auth/email-verifications", { email: email.trim(), purpose });
      if (requestVersion.current !== version) return;
      setSent(true);
      setVerified(false);
      setCode("");
      setExpiresAt(Date.now() + 300000);
      setMessage("인증번호를 발송했습니다. 이메일을 확인해 주세요.");
      onChange?.({ email: email.trim(), verified: false, code: "" });
    } catch (error) {
      if (requestVersion.current !== version) return;
      setMessage(error.response?.data?.error?.message || "인증번호를 발송하지 못했습니다. 다시 시도해 주세요.");
    } finally {
      if (requestVersion.current === version) setBusy(false);
    }
  }

  // AI작성: 현재 이메일과 인증 목적에 맞는 6자리 코드를 서버에서 확인한다.
  async function verifyCode() {
    if (!/^[0-9]{6}$/.test(code)) {
      setMessage("6자리 숫자를 입력해 주세요.");
      return;
    }
    const version = ++requestVersion.current;
    setBusy(true);
    setMessage("");
    try {
      await client.post("/auth/email-verifications/verify", { email: email.trim(), purpose, code });
      if (requestVersion.current !== version) return;
      setVerified(true);
      setMessage("이메일 인증이 완료되었습니다.");
      onChange?.({ email: email.trim(), verified: true, code });
    } catch (error) {
      if (requestVersion.current !== version) return;
      setVerified(false);
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
        <input type="email" placeholder="writer@example.com" value={email} onChange={changeEmail} autoComplete="email" />
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
      <small>{sent ? verified ? "인증이 완료되었습니다." : `남은 시간 ${minutes}:${seconds}` : "인증번호는 5분 동안 유효합니다."}</small>
      {message && <small role="status">{message}</small>}
    </>
  );
}
