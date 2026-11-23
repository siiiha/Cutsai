# Cutsai 프론트엔드

PDF 「제목 없음」 기준 20페이지를 원본 순서에 맞춰 React 화면으로 구성했다. `src/screens`에 화면별 JSX가 있고, 공통 스타일은 `src/styles.css`에 있다. 화면은 `/login`, `/episodes`처럼 경로로 접근하며, 없는 주소는 404 화면을 보여준다. 기존 `?page=1`부터 `?page=20` 주소도 해당 화면 경로로 이동한다. 작업 화면은 `src/components/WorkLayout.jsx`의 공통 사이드 메뉴를 사용한다.

## 실행

1. Node.js가 설치된 환경에서 이 폴더를 연다.
2. `npm install`
3. `npm run dev`
4. 터미널에 표시된 주소를 열고 화면을 확인한다.

프론트엔드의 `/api` 요청은 개발 중 `http://localhost:8080` 백엔드로 전달된다. API 요청은 `src/api/client.js`를 공통 클라이언트로 사용하고, 기능별 API 함수는 백엔드 API가 정해지면 `src/api`에 추가한다.

`npm run build`로 `dist`에 배포용 파일을 생성할 수 있다.

일부 버튼은 PDF의 화면 흐름을 확인할 수 있게 연결했다. 인증, 저장, 점검 및 업로드를 수행하는 서버 기능은 포함하지 않는다. 표현은 1440×900에서 확인하는 것을 기준으로 하며, 설치 글꼴과 PDF·브라우저의 렌더링 차이로 픽셀 단위 차이가 생길 수 있다.
