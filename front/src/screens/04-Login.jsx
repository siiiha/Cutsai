import { Link } from "react-router-dom";

export default function Login() {
  return (
    <>
      <div className={"auth"}>
        <div className={"card "}>
          <div className={"brand"}>
            <span className={"mark"}>{"✎"}</span>
            {"작업실"}
          </div>
          <h1>{"로그인"}</h1>
          <div className={"form"}>
            <label className={"field"}>
              {"이메일 아이디"}
              <input placeholder={"writer@example.com"} />
            </label>
            <label className={"field"}>
              {"비밀번호"}
              <input placeholder={"비밀번호 입력"} />
              <span className={"eye"}></span>
            </label>
            <button className={"primary"}>{"로그인"}</button>
          </div>
          <div className={"foot"}><Link to="/signup">회원가입</Link>{"　·　"}<Link to="/password/reset">비밀번호 찾기</Link></div>
        </div>
      </div>
    </>
  );
}
