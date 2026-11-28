-- AI작성: 실행 전 대상 데이터베이스와 외래 키 이름을 확인한다.
-- AI작성: 이 파일은 기존 테이블의 외래 키를 변경하며 자동 실행되지 않는다.
-- AI작성: MySQL DDL은 암묵적 커밋이 발생할 수 있으므로 두 테이블 변경이 하나의 트랜잭션으로 묶이지 않는다.

-- AI작성: 회원 삭제 시 소유한 회차 일정이 함께 삭제되도록 변경한다.
ALTER TABLE episode_schedule
    DROP FOREIGN KEY fk_episode_schedule_member,
    ADD CONSTRAINT fk_episode_schedule_member_cascade
        FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE;

-- AI작성: 회원 삭제 시 해당 리프레시 토큰 기록이 함께 삭제되도록 변경한다.
ALTER TABLE refresh_token
    DROP FOREIGN KEY fk_refresh_token_member,
    ADD CONSTRAINT fk_refresh_token_member_cascade
        FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE;
