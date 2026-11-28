-- AI작성: 실행 전에 대상 데이터베이스와 member 테이블의 기존 컬럼을 확인한다.
-- AI작성: 기존 회원은 활성 상태와 토큰 버전 0으로 유지된다.
-- AI작성: MySQL DDL은 암묵적 커밋을 발생시킬 수 있으며 이 파일은 자동 실행되지 않는다.
-- AI작성: 이미 존재하는 컬럼은 다시 추가하지 않는다. 컬럼의 기존 타입은 변경하지 않으므로 실행 전 확인한다.

-- AI작성: 탈퇴 요청 시각 컬럼이 없을 때만 추가한다.
SET @cutsai_withdrawal_column_sql = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE member ADD COLUMN withdrawn_at DATETIME NULL AFTER password_hash',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND COLUMN_NAME = 'withdrawn_at'
);
PREPARE cutsai_withdrawal_column_statement FROM @cutsai_withdrawal_column_sql;
EXECUTE cutsai_withdrawal_column_statement;
DEALLOCATE PREPARE cutsai_withdrawal_column_statement;

-- AI작성: 탈퇴 전 액세스 토큰을 구분할 버전 컬럼이 없을 때만 추가한다.
SET @cutsai_token_version_column_sql = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE member ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER withdrawn_at',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member' AND COLUMN_NAME = 'token_version'
);
PREPARE cutsai_token_version_column_statement FROM @cutsai_token_version_column_sql;
EXECUTE cutsai_token_version_column_statement;
DEALLOCATE PREPARE cutsai_token_version_column_statement;
