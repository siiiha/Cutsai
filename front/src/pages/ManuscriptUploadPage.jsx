export default function ManuscriptUpload() {
  return (
    <>

      <div className={"workspace"}>
        <main className={"main"}>
          <header className={"head"}>
            <h1>{"원고 미리보기"}</h1>
            <p>{"여러 원고 이미지를 추가한 뒤 읽는 순서대로 정리하세요."}</p>
          </header>
          <div className={"pair"}>
            <div className={"card "}>
              <h2>{"원고 이미지 추가"}</h2>
              <div className={"drop"}>
                <h2>{"이미지 파일을 끌어다 놓으세요"}</h2>
                <button className={""}>{"파일 선택"}</button>
              </div>
            </div>
            <div className={"card "}>
              <h2>{"페이지 순서"}</h2>
              <small>{"끌어서 순서를 바꿀 수 있어요."}</small>
              <div className={"row file spread"}>
                <b>{"⠿　01"}</b>
                <span>{"page-01.png"}</span>
                <span>{"×"}</span>
              </div>
              <div className={"row file spread"}>
                <b>{"⠿　02"}</b>
                <span>{"page-02.png"}</span>
                <span>{"×"}</span>
              </div>
              <div className={"row file spread"}>
                <b>{"⠿　03"}</b>
                <span>{"page-03.png"}</span>
                <span>{"×"}</span>
              </div>
            </div>
          </div>
          <br />
          <div className={"row"} style={{"justifyContent":"flex-end"}}>
            <button className={"primary"}>{"모바일로 미리보기 →"}</button>
          </div>
        </main>
      </div>
    </>
  );
}
