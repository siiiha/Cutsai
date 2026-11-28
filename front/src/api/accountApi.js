import client from "./client.js";

// AI작성: 현재 계정 정보를 조회한다.
export function getAccount() { return client.get("/account"); }

// AI작성: 계정의 닉네임을 변경한다.
export function updateNickname(nickname) { return client.patch("/account/nickname", { nickname }); }

// AI작성: 현재 비밀번호를 확인하고 새 비밀번호로 변경한다.
export function updatePassword(currentPassword, newPassword) { return client.patch("/account/password", { currentPassword, newPassword }); }

// AI작성: 현재 비밀번호를 전달하여 회원 탈퇴를 요청한다.
export function deleteAccount(currentPassword) { return client.delete("/account", { data: { currentPassword } }); }
