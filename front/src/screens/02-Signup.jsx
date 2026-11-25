import { useState } from "react";
import { Link } from "react-router-dom";
import EmailVerification from "../components/EmailVerification.jsx";

export default function Signup() {
  // AI작성: 인증된 이메일과 코드는 가입 요청을 연결할 때 사용한다.
  const [verification, setVerification] = useState({ email: "", verified: false, code: "" });
  return (
    <>
      <div className={"auth"}>
        <div className={"card "}>
          <div className={"brand"}>
            <span className={"mark"}>{"✎"}</span>
            {"Cutsai"}
          </div>
          <h1>{"회원가입"}</h1>
          <div className={"form"}>
            {/* AI작성: 회원가입 목적의 이메일 인증을 공통 구성요소로 처리한다. */}
            <EmailVerification purpose="SIGNUP" onChange={setVerification} />
            <label className={"field"}>
              {"닉네임"}
              <input placeholder={"사용할 닉네임 입력"} />
            </label>
            <label className={"field"}>
              {"비밀번호"}
              <input placeholder={"8~16자 · 영문, 숫자, 특수문자 사용 가능"} />
              <span className={"eye"}></span>
            </label>
            <label className={"field"}>
              {"비밀번호 확인"}
              <input placeholder={"비밀번호 다시 입력"} />
              <span className={"eye"}></span>
            </label>
            <button className={"primary"} disabled={!verification.verified}>{"가입하기"}</button>
          </div>
          <div className={"foot"}>{"이미 계정이 있나요?　"}<Link to="/login">로그인</Link></div>
        </div>
      </div>
    </>
  );
}
