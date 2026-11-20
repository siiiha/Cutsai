export default function Dashboard() {
  return (
    <>
      <aside className={"side"}>
        <div className={"brand"}>
          <span className={"mark"}>{"✎"}</span>
          {"작업실"}
        </div>
        <nav>
          <div className={"on"}>
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
              <h1>{"대시보드"}</h1>
              <p>{"회차의 작업 단계와 마감 일정을 확인하세요."}</p>
            </header>
            <button className={""}>{"◐"}</button>
          </div>
          <div className={"stats"}>
            <div className={"card stat"}>
              {"진행 중"}
              <b>{"5"}</b>
            </div>
            <div className={"card stat"}>
              {"마감 임박"}
              <b>{"1"}</b>
            </div>
            <div className={"card stat"}>
              {"지연"}
              <b>{"1"}</b>
            </div>
          </div>
          <h2>{"회차 작업 현황"}</h2>
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
                </tr>
              </tbody>
            </table>
          </div>
        </main>
      </div>
    </>
  );
}
