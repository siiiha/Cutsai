import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "../api/authApi.js";
import { getAccount, updateNickname, updatePassword } from "../api/accountApi.js";
import { useAuthStore } from "../stores/useAuthStore.js";

// AI작성: 계정 정보를 조회하고 닉네임과 비밀번호 설정을 변경한다.
export default function Settings() {
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [nickname, setNickname] = useState("");
  const [editingNickname, setEditingNickname] = useState(false);
  const [editingPassword, setEditingPassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [reload, setReload] = useState(0);

  // AI작성: 화면에 표시할 현재 계정 정보를 불러온다.
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    getAccount().then((response) => {
      if (!active) return;
      setAccount(response.data.data);
      setNickname(response.data.data.nickname);
    }).catch((requestError) => {
      if (active) setError(requestError.response?.data?.error?.message || "계정 정보를 불러오지 못했습니다.");
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [reload]);

  // AI작성: 닉네임 길이를 확인하고 서버에서 저장한 값을 화면과 인증 상태에 반영한다.
  async function saveNickname(event) {
    event.preventDefault();
    if (busy) return;
    const next = nickname.trim();
    if (next.length < 2 || next.length > 12) {
      setError("닉네임은 2~12자로 입력해 주세요.");
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      const response = await updateNickname(next);
      const saved = response.data.data.nickname;
      setAccount((current) => ({ ...current, nickname: saved }));
      const auth = useAuthStore.getState();
      if (auth.member) auth.setAuth({ accessToken: auth.accessToken, member: { ...auth.member, nickname: saved } });
      setNickname(saved);
      setEditingNickname(false);
      setNotice("닉네임이 변경되었습니다.");
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "닉네임을 변경하지 못했습니다.");
    } finally {
      setBusy(false);
    }
  }

  // AI작성: 현재 비밀번호와 새 비밀번호를 확인하고 변경 성공 후 다시 로그인하도록 한다.
  async function savePassword(event) {
    event.preventDefault();
    if (busy) return;
    if (!currentPassword || newPassword.length < 8 || newPassword.length > 16
      || !/[A-Za-z]/.test(newPassword) || !/[0-9]/.test(newPassword) || !/[\p{P}\p{S}]/u.test(newPassword)) {
      setError("현재 비밀번호와 영문·숫자·특수문자를 포함한 8~16자의 새 비밀번호를 확인해 주세요.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("새 비밀번호 확인이 일치하지 않습니다.");
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      const response = await updatePassword(currentPassword, newPassword);
      if (response.status !== 204) throw new Error("비밀번호 변경 결과를 확인하지 못했습니다.");
      useAuthStore.getState().clearAuth();
      navigate("/login", { replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "비밀번호를 변경하지 못했습니다.");
    } finally {
      setBusy(false);
    }
  }

  // AI작성: 로그아웃 요청이 성공하면 인증 상태를 정리하고 첫 화면으로 이동한다.
  async function handleLogout(event) {
    event.stopPropagation();
    if (busy) return;
    setBusy(true);
    setError("");
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (requestError) {
      setError(requestError.response?.data?.error?.message || "로그아웃을 처리하지 못했습니다.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="workspace">
      <main className="main">
        <div className="setting stack">
          <header className="head"><h1>설정</h1></header>
          {loading && <p role="status">계정 정보를 불러오는 중입니다.</p>}
          {error && <p role="alert">{error}</p>}
          {notice && <p role="status">{notice}</p>}
          {!loading && !account && <button type="button" onClick={() => setReload((value) => value + 1)}>다시 시도</button>}
          {account && <>
            <div className="card">
              <h2>프로필</h2>
              {!editingNickname ? (
                <div className="row spread">
                  <b>닉네임</b><span>{account.nickname}</span>
                  <button type="button" onClick={() => { setError(""); setEditingNickname(true); }}>수정</button>
                </div>
              ) : (
                <form className="form" onSubmit={saveNickname}>
                  <label className="field">닉네임<input value={nickname} onChange={(event) => setNickname(event.target.value)} required /></label>
                  <div className="actions">
                    <button type="button" disabled={busy} onClick={() => { setNickname(account.nickname); setEditingNickname(false); setError(""); }}>취소</button>
                    <button type="submit" disabled={busy}>저장</button>
                  </div>
                </form>
              )}
            </div>
            <div className="card">
              <h2>계정 설정</h2>
              <div className="row spread"><b>이메일 아이디</b><span>{account.email}</span><span className="badge">변경 불가</span></div>
              {!editingPassword ? (
                <div className="row spread">
                  <b>비밀번호</b>
                  {/* AI작성: 시안의 공통 화면 전환을 막고 현재 비밀번호 입력란을 연다. */}
                  <button type="button" onClick={(event) => { event.stopPropagation(); setError(""); setEditingPassword(true); }}>변경</button>
                </div>
              ) : (
                <form className="form" onSubmit={savePassword}>
                  <label className="field">현재 비밀번호<input type="password" autoComplete="current-password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} required /></label>
                  <label className="field">새 비밀번호<input type="password" autoComplete="new-password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} required /></label>
                  <label className="field">새 비밀번호 확인<input type="password" autoComplete="new-password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} required /></label>
                  <small>새 비밀번호는 8~16자이며 영문·숫자·특수문자를 각각 포함해야 합니다.</small>
                  <div className="actions">
                    <button type="button" disabled={busy} onClick={() => { setEditingPassword(false); setCurrentPassword(""); setNewPassword(""); setConfirmPassword(""); setError(""); }}>취소</button>
                    <button type="submit" disabled={busy}>비밀번호 저장</button>
                  </div>
                </form>
              )}
            </div>
            <div className="card">
              <h2>기타</h2>
              <div className="row">
                <button type="button" disabled={busy} onClick={handleLogout}>로그아웃</button>
                <Link className="danger" to="/withdrawal">회원 탈퇴</Link>
              </div>
            </div>
          </>}
        </div>
      </main>
    </div>
  );
}
