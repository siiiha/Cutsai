import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import client from "../api/client.js";
import { useAuthStore } from "../stores/useAuthStore.js";
import Settings from "./09-Settings.jsx";

// AI작성: 회원 탈퇴 안내 확인과 현재 비밀번호 입력을 처리한다.
export default function Withdrawal() {
  const navigate = useNavigate();
  const [confirmed, setConfirmed] = useState(false);
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  // AI작성: 안내 확인과 비밀번호를 검증하고 탈퇴 성공 시 인증 상태를 정리한다.
  async function submit(event) {
    event.preventDefault();
    if (submitting) return;
    if (!confirmed || !password) {
      setError("탈퇴 안내를 확인하고 현재 비밀번호를 입력해 주세요.");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const response = await client.delete("/account", { data: { currentPassword: password } });
      if (response.status !== 204) throw new Error("탈퇴 결과를 확인하지 못했습니다.");
      useAuthStore.getState().clearAuth();
      navigate("/", { replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "회원 탈퇴를 처리하지 못했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Settings />
      <div className="modalshade">
        <div className="card modal" role="dialog" aria-modal="true" aria-labelledby="withdrawal-title">
          <h2 id="withdrawal-title">회원 탈퇴</h2>
          <div className="notice">
            <b>탈퇴 전 확인해 주세요</b>
            <ul>
              <li>탈퇴가 완료되면 계정과 등록한 회차 일정이 삭제됩니다.</li>
              <li>삭제된 계정과 회차 일정은 복구할 수 없습니다.</li>
              <li>개인정보 처리 기준은 개인정보 처리방침에서 확인할 수 있습니다.</li>
            </ul>
          </div>
          <form className="form" onSubmit={submit}>
            <label>
              <input type="checkbox" checked={confirmed} onChange={(event) => setConfirmed(event.target.checked)} />
              탈퇴 안내를 확인했습니다.
            </label>
            <label className="field">
              현재 비밀번호
              <input type="password" autoComplete="current-password" value={password}
                onChange={(event) => setPassword(event.target.value)} required />
            </label>
            {error && <p role="alert">{error}</p>}
            <div className="actions">
              <Link to="/settings">취소</Link>
              <button className="danger" type="submit" disabled={!confirmed || !password || submitting}>
                {submitting ? "처리 중..." : "탈퇴하기"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
}
