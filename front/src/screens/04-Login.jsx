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
          <div className={"foot"}><a href="?page=2" data-page="2">회원가입</a>{"　·　"}<a href="?page=6" data-page="6">비밀번호 찾기</a></div>
        </div>
      </div>
    </>
  );
}
