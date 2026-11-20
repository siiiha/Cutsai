export default function SignupComplete() {
  return (
    <>
      <div className={"auth"}>
        <div className={"card "}>
          <div className={"brand"}>
            <span className={"mark"}>{"✎"}</span>
            {"작업실"}
          </div>
          <h1>{"회원가입"}</h1>
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
            <button className={"primary"}>{"가입하기"}</button>
          </div>
          <div className={"foot"}>{"이미 계정이 있나요?　로그인"}</div>
        </div>
      </div>
      <div className={"modalshade"}>
        <div className={"card modal success"}>
          <div className={"check"}>{"✓"}</div>
          <h2>{"가입이 완료됐어요"}</h2>
          <p>{"이제 작업실에 로그인할 수 있어요."}</p>
          <br />
          <button className={"primary"}>{"로그인으로 이동"}</button>
        </div>
      </div>
    </>
  );
}
