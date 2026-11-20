export default function PasswordReset() {
  return (
    <>
      <div className={"auth"}>
        <div className={"card "}>
          <div className={"brand"}>
            <span className={"mark"}>{"✎"}</span>
            {"작업실"}
          </div>
          <h1>{"비밀번호 재설정"}</h1>
          <div className={"form"}>
            <label className={"field"}>
              {"이메일 아이디"}
              <input placeholder={"writer@example.com"} />
            </label>
            <div className={"row "}>
              <input placeholder={"6자리 인증번호"} />
              <button className={""}>{"인증번호 발송"}</button>
            </div>
            <small>{"인증번호는 5분 동안 유효합니다."}</small>
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
            <button className={"primary"}>{"비밀번호 변경"}</button>
          </div>
          <div className={"foot"}><a href="?page=4" data-page="4">로그인으로 돌아가기</a></div>
        </div>
      </div>
    </>
  );
}
