export default function Withdrawal() {
  return (
    <>

      <div className={"workspace"}>
        <main className={"main"}>
          <div className={"setting stack"}>
            <header className={"head"}>
              <h1>{"설정"}</h1>
            </header>
            <div className={"card "}>
              <h2>{"프로필"}</h2>
              <div className={"row spread"}>
                <b>{"닉네임"}</b>
                <span>{"작업실지기"}</span>
                <button className={""}>{"수정"}</button>
              </div>
            </div>
            <div className={"card "}>
              <h2>{"계정 설정"}</h2>
              <div className={"row spread"}>
                <b>{"이메일 아이디"}</b>
                <span>{"writer@example.com"}</span>
                <span className={"badge "}>{"변경 불가"}</span>
              </div>
              <div className={"row spread"}>
                <b>{"비밀번호"}</b>
                <button className={""}>{"변경"}</button>
              </div>
            </div>
            <div className={"card "}>
              <h2>{"기타"}</h2>
              <div className={"row "}>
                <button className={""}>{"로그아웃"}</button>
                <button className={"danger"}>{"회원 탈퇴"}</button>
              </div>
            </div>
          </div>
        </main>
      </div>
      <div className={"modalshade"}>
        <div className={"card modal "}>
          <h2>{"회원 탈퇴"}</h2>
          <div className={"notice"}>
            <b>{"탈퇴 전 확인해 주세요"}</b>
            <ul>
              <li>{"탈퇴가 완료되면 계정 이용이 종료됩니다."}</li>
              <li>{"등록한 회차 일정은 탈퇴한 계정에서 확인할 수 없습니다."}</li>
              <li>{"개인정보 처리 기준은 개인정보 처리방침에서 확인할 수 있습니다."}</li>
            </ul>
          </div>
          <p>{"□　탈퇴 안내를 확인했습니다."}</p>
          <br />
          <label className={"field"}>
            {"비밀번호"}
            <input placeholder={"비밀번호 입력"} />
            <span className={"eye"}></span>
          </label>
          <div className={"actions"}>
            <button className={""}>{"취소"}</button>
            <button className={"danger"}>{"탈퇴하기"}</button>
          </div>
        </div>
      </div>
    </>
  );
}
