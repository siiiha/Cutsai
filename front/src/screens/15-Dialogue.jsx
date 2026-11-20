export default function Dialogue() {
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
          <div className={"on"}>
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
          <div className={""}>
            <i>{"⚙"}</i>
            {"설정"}
          </div>
        </nav>
      </aside>
      <div className={"workspace"}>
        <main className={"main"}>
          <header className={"head"}>
            <h1>{"대사 점검"}</h1>
            <p>{"맞춤법, 가독성, 대사 쪼개기, 줄바꿈을 한 번에 확인하세요."}</p>
          </header>
          <div className={"row "}>
            <button className={"soft"}>{"콘티"}</button>
            <button className={""}>{"완성"}</button>
          </div>
          <br />
          <div className={"pair"}>
            <div className={"card "}>
              <h2>{"대사 입력"}</h2>
              <div className={"textarea"}>{"점검할 대사를 입력하세요."}</div>
              <br />
              <div className={"row spread"}>
                <small>{"입력한 대사는 저장되지 않습니다."}</small>
                <button className={"primary"}>{"점검하기"}</button>
              </div>
            </div>
            <div className={"card "}>
              <h2>{"점검 결과"}</h2>
              <p>{"대사를 입력하고 점검을 시작하면 제안이 표시됩니다."}</p>
            </div>
          </div>
        </main>
      </div>
    </>
  );
}
