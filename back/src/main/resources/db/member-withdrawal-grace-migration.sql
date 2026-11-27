-- AI작성: 실행 전에 대상 데이터베이스와 member 테이블의 기존 컬럼을 확인한다.
-- AI작성: 기존 회원은 withdrawn_at이 NULL인 활성 상태로 유지된다.
-- AI작성: MySQL DDL은 암묵적 커밋을 발생시킬 수 있으며 이 파일은 자동 실행되지 않는다.
ALTER TABLE member
    ADD COLUMN withdrawn_at DATETIME NULL AFTER password_hash;
