package com.siha.cutsai.auth;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** AI작성: 계정·IP별 로그인 실패 제한 데이터를 관리한다. */
@Mapper
public interface LoginRateLimitMapper {
    /** AI작성: 제한 행이 없으면 생성하고 있으면 기존 값을 유지한다. */
    @Insert("""
            INSERT INTO login_rate_limit (limit_scope, subject_hash, updated_at)
            VALUES (#{scope}, #{hash}, UTC_TIMESTAMP()) ON DUPLICATE KEY UPDATE failure_count = failure_count
            """)
    int insertIfAbsent(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 실패 횟수 갱신을 위해 제한 행을 잠근다. */
    @Select("""
            SELECT failure_count, locked_until FROM login_rate_limit
            WHERE limit_scope = #{scope} AND subject_hash = #{hash} FOR UPDATE
            """)
    Limit findForUpdate(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 실패 횟수와 잠금 종료 시각을 저장한다. */
    @Update("""
            UPDATE login_rate_limit SET failure_count = #{failures}, locked_until = #{lockedUntil}, updated_at = #{updatedAt}
            WHERE limit_scope = #{scope} AND subject_hash = #{hash}
            """)
    int update(@Param("scope") String scope, @Param("hash") String hash, @Param("failures") int failures,
               @Param("lockedUntil") LocalDateTime lockedUntil, @Param("updatedAt") LocalDateTime updatedAt);

    /** AI작성: 로그인 성공 시 해당 제한 행을 제거한다. */
    @Delete("DELETE FROM login_rate_limit WHERE limit_scope = #{scope} AND subject_hash = #{hash}")
    int delete(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 로그인 실패 제한 조회 결과다. */
    record Limit(int failures, LocalDateTime lockedUntil) {}
}
