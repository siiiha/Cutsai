export default function Settings() {
  return (
    <>
      <aside className={"side"}>
        <div className={"brand"}>
          <span className={"mark"}>{"✎"}</span>
          {"작업실"}
        </div>
        <nav>
          <div className={""}>
            <i>{"▤"}</i>
            {"대시보드"}
          </div>
          <div className={""}>
            <i>{"▱"}</i>
            {"회차 관리"}
          </div>
          <div className={""}>
            <i>{"◇"}</i>
            {"대사 점검"}
          </div>
          <div className={""}>
            <i>{"✦"}</i>
            {"효과음 소재"}
          </div>
          <div className={""}>
            <i>{"▧"}</i>
            {"원고 미리보기"}
          </div>
          <div className={"on"}>
            <i>{"⚙"}</i>
            {"설정"}
          </div>
        </nav>
      </aside>
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
    </>
  );
}
