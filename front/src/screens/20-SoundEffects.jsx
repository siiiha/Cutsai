export default function SoundEffects() {
  return (
    <>

      <div className={"workspace"}>
        <main className={"main"}>
          <header className={"head"}>
            <h1>{"효과음 소재 검색"}</h1>
            <p>{"웹툰에 사용할 효과음 이미지를 찾아보세요."}</p>
          </header>
          <div className={"row "}>
            <input defaultValue={"쾅"} />
            <button className={"primary"}>{"검색"}</button>
          </div>
          <br />
          <h2>{"검색 결과 5개"}</h2>
          <div className={"gallery"}>
            <div className={"card "}>
              <div className={"thumb"}>
                <b style={{"--c":"#1d2924","--a":"-8deg"}}>{"쾅!"}</b>
              </div>
              <p>{"묵직한 충격"}</p>
              <div className={"row "}>
                <button className={""}>{"미리보기"}</button>
                <button className={"soft"}>{"다운로드"}</button>
              </div>
            </div>
            <div className={"card "}>
              <div className={"thumb"}>
                <b style={{"--c":"#533d34","--a":"-16deg"}}>{"쾅!"}</b>
              </div>
              <p>{"먹선"}</p>
              <div className={"row "}>
                <button className={""}>{"미리보기"}</button>
                <button className={"soft"}>{"다운로드"}</button>
              </div>
            </div>
            <div className={"card "}>
              <div className={"thumb"}>
                <b style={{"--c":"#a13234","--a":"9deg"}}>{"쾅!"}</b>
              </div>
              <p>{"붉은 충돌"}</p>
              <div className={"row "}>
                <button className={""}>{"미리보기"}</button>
                <button className={"soft"}>{"다운로드"}</button>
              </div>
            </div>
            <div className={"card "}>
              <div className={"thumb"}>
                <b style={{"--c":"#513f76","--a":"-10deg"}}>{"쾅!"}</b>
              </div>
              <p>{"속도선"}</p>
              <div className={"row "}>
                <button className={""}>{"미리보기"}</button>
                <button className={"soft"}>{"다운로드"}</button>
              </div>
            </div>
            <div className={"card "}>
              <div className={"thumb"}>
                <b style={{"--c":"#315398","--a":"12deg"}}>{"쾅!"}</b>
              </div>
              <p>{"푸른 섬광"}</p>
              <div className={"row "}>
                <button className={""}>{"미리보기"}</button>
                <button className={"soft"}>{"다운로드"}</button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </>
  );
}
