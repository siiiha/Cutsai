export default function DialogueResults() {
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
              <div className={"textarea"}>
                {"난 할수 있어!"}
                <br />
                {"오늘은 비가 많이 와서 그냥 조금 더 있다가 가자."}
              </div>
              <br />
              <div className={"row spread"}>
                <small>{"입력한 대사는 저장되지 않습니다."}</small>
                <button className={"primary"}>{"다시 점검"}</button>
              </div>
            </div>
            <div className={"card "}>
              <h2>{"점검 결과"}</h2>
              <div className={"row result spread"}>
                <div>
                  <b>{"맞춤법·띄어쓰기"}</b>
                  <p>{"할수 있어 → 할 수 있어"}</p>
                </div>
                <button className={""}>{"적용"}</button>
              </div>
              <div className={"row result spread"}>
                <div>
                  <b>{"가독성"}</b>
                  <p>{"문장이 길어요. 한 호흡에 읽을 수 있게 다듬어 보세요."}</p>
                </div>
                <button className={""}>{"적용"}</button>
              </div>
              <div className={"row result spread"}>
                <div>
                  <b>{"대사 쪼개기"}</b>
                  <p>
                    <span className={"bubble"}>{"비가 많이 오네."}</span>
                    {" "}
                    <span className={"bubble"}>{"조금 더 있다가 가자."}</span>
                  </p>
                </div>
                <button className={""}>{"적용"}</button>
              </div>
              <div className={"row result spread"}>
                <div>
                  <b>{"줄바꿈"}</b>
                  <p>
                    <span className={"bubble"}>
                      {"오늘은 비가 많이 와서"}
                      <br />
                      {"그냥 조금 더 있다가 가자."}
                    </span>
                  </p>
                </div>
                <button className={""}>{"적용"}</button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </>
  );
}
