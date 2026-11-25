import { useState } from "react";
import { Link } from "react-router-dom";
import EmailVerification from "../components/EmailVerification.jsx";

export default function PasswordReset() {
  // AI작성: 인증된 이메일과 코드는 비밀번호 재설정 요청에 사용한다.
  const [verification, setVerification] = useState({ email: "", verified: false, code: "" });
  return (
    <>
      <div className={"auth"}>
        <div className={"card "}>
          <div className={"brand"}>
            <span className={"mark"}>{"✎"}</span>
            {"Cutsai"}
          </div>
          <h1>{"비밀번호 재설정"}</h1>
          <div className={"form"}>
            {/* AI작성: 비밀번호 재설정 목적의 이메일 인증을 공통 구성요소로 처리한다. */}
            <EmailVerification purpose="PASSWORD_RESET" onChange={setVerification} />
            <label className={"field"}>
              {"새 비밀번호"}
              <input placeholder={"8~16자 · 영문, 숫자, 특수문자 사용 가능"} />
              <span className={"eye"}></span>
            </label>
            <label className={"field"}>
              {"새 비밀번호 확인"}
              <input placeholder={"비밀번호 다시 입력"} />
              <span className={"eye"}></span>
            </label>
            <button className={"primary"} disabled={!verification.verified}>{"비밀번호 변경"}</button>
          </div>
          <div className={"foot"}><Link to="/login">로그인으로 돌아가기</Link></div>
        </div>
      </div>
    </>
  );
}
