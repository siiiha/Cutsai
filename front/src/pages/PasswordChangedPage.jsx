import { Navigate, useLocation } from "react-router-dom";

export default function PasswordChanged() {
  // AI작성: 변경 성공을 거치지 않은 완료 화면 접근은 재설정 화면으로 돌려보낸다.
  const location = useLocation();
  if (!location.state?.passwordResetCompleted) return <Navigate to="/password/reset" replace />;
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
          <div className={"foot"}>{"로그인으로 돌아가기"}</div>
        </div>
      </div>
      <div className={"modalshade"}>
        <div className={"card modal success"}>
          <div className={"check"}>{"✓"}</div>
          <h2>{"비밀번호 변경 완료"}</h2>
          <p>{"새 비밀번호로 로그인해 주세요."}</p>
          <br />
          <button className={"primary"}>{"로그인으로 이동"}</button>
        </div>
      </div>
    </>
  );
}
