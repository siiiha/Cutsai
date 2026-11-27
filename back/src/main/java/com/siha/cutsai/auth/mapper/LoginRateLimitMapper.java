package com.siha.cutsai.auth.mapper;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** AI작성: 계정·IP별 로그인 실패 제한 데이터를 관리한다. */
@Mapper
public interface LoginRateLimitMapper {
    /** AI작성: 제한 행이 없으면 생성하고 있으면 기존 값을 유지한다. */
    int insertIfAbsent(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 실패 횟수 갱신을 위해 제한 행을 잠근다. */
    Limit findForUpdate(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 실패 횟수와 잠금 종료 시각을 저장한다. */
    int update(@Param("scope") String scope, @Param("hash") String hash, @Param("failures") int failures,
               @Param("lockedUntil") LocalDateTime lockedUntil, @Param("updatedAt") LocalDateTime updatedAt);

    /** AI작성: 로그인 성공 시 해당 제한 행을 제거한다. */
    int delete(@Param("scope") String scope, @Param("hash") String hash);

    /** AI작성: 로그인 실패 제한 조회 결과다. */
    record Limit(int failures, LocalDateTime lockedUntil) {}
}
