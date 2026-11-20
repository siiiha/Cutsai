export default function Episodes() {
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
          <div className={"on"}>
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
          <div className={""}>
            <i>{"⚙"}</i>
            {"설정"}
          </div>
        </nav>
      </aside>
      <div className={"workspace"}>
        <main className={"main"}>
          <div className={"row spread"}>
            <header className={"head"}>
              <h1>{"회차 관리"}</h1>
              <p>{"작업 단계와 마감일을 관리하세요."}</p>
            </header>
            <button className={"primary"}>{"＋ 회차 등록"}</button>
          </div>
          <div className={"row "}>
            <input style={{"maxWidth":"250px"}} placeholder={"회차명 검색"} />
            <button className={"soft"}>{"전체"}</button>
            <button className={""}>{"콘티"}</button>
            <button className={""}>{"선화"}</button>
            <button className={""}>{"채색"}</button>
            <button className={""}>{"후보정"}</button>
            <button className={""}>{"편집"}</button>
          </div>
          <br />
          <div className={"card table"}>
            <table>
              <thead>
                <tr>
                  <th>{"회차명"}</th>
                  <th>{"작업 단계"}</th>
                  <th>{"마감일"}</th>
                  <th>{"남은 기간"}</th>
                  <th>{"진행률"}</th>
                  <th>{"상태"}</th>
                  <th>{"관리"}</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{"11화 · 약속"}</td>
                  <td>{"채색"}</td>
                  <td>{"11.17"}</td>
                  <td>{"D+2"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"40%"}}></i>
                    </span>
                    {"40%"}
                  </td>
                  <td>
                    <span className={"badge late"}>{"지연"}</span>
                  </td>
                  <td>{"수정　"}<a href="?page=13" data-page="13" aria-label="회차 삭제 확인" style={{color:"inherit",textDecoration:"none"}}>⋯</a></td>
                </tr>
                <tr>
                  <td>{"12화 · 비 오는 오후"}</td>
                  <td>{"선화"}</td>
                  <td>{"11.19"}</td>
                  <td>{"D-day"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"20%"}}></i>
                    </span>
                    {"20%"}
                  </td>
                  <td>
                    <span className={"badge soon"}>{"마감 임박"}</span>
                  </td>
                  <td>{"수정　⋯"}</td>
                </tr>
                <tr>
                  <td>{"13화 · 새로운 장면"}</td>
                  <td>{"콘티"}</td>
                  <td>{"11.25"}</td>
                  <td>{"D-6"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"0%"}}></i>
                    </span>
                    {"0%"}
                  </td>
                  <td>
                    <span className={"badge "}>{"진행 중"}</span>
                  </td>
                  <td>{"수정　⋯"}</td>
                </tr>
                <tr>
                  <td>{"14화 · 편지"}</td>
                  <td>{"후보정"}</td>
                  <td>{"11.27"}</td>
                  <td>{"D-8"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"60%"}}></i>
                    </span>
                    {"60%"}
                  </td>
                  <td>
                    <span className={"badge "}>{"진행 중"}</span>
                  </td>
                  <td>{"수정　⋯"}</td>
                </tr>
                <tr>
                  <td>{"15화 · 다시 만난 날"}</td>
                  <td>{"편집"}</td>
                  <td>{"11.29"}</td>
                  <td>{"D-10"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"80%"}}></i>
                    </span>
                    {"80%"}
                  </td>
                  <td>
                    <span className={"badge "}>{"진행 중"}</span>
                  </td>
                  <td>{"수정　⋯"}</td>
                </tr>
                <tr>
                  <td>{"10화 · 첫 만남"}</td>
                  <td>{"편집"}</td>
                  <td>{"미정"}</td>
                  <td>{"—"}</td>
                  <td>
                    <span className={"meter"}>
                      <i style={{"width":"100%"}}></i>
                    </span>
                    {"100%"}
                  </td>
                  <td>
                    <span className={"badge "}>{"완료"}</span>
                  </td>
                  <td>{"수정　⋯"}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </main>
      </div>
    </>
  );
}
