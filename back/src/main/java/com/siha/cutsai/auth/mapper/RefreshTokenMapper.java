package com.siha.cutsai.auth.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** AI작성: refresh_token 운영 데이터를 조회·폐기·저장한다. */
@Mapper
public interface RefreshTokenMapper {
    /** AI작성: 재발급 대상 토큰을 행 잠금으로 조회한다. */
    @Select("""
            SELECT expires_at, revoked_at FROM refresh_token
            WHERE token_hash = #{tokenHash} AND member_id = #{memberId} FOR UPDATE
            """)
    Optional<RefreshTokenSession> findForUpdate(@Param("memberId") long memberId,
                                                @Param("tokenHash") String tokenHash);

    /** AI작성: 사용된 refresh 토큰을 폐기 상태로 전환한다. */
    @Update("""
            UPDATE refresh_token SET revoked_at = #{revokedAt}
            WHERE token_hash = #{tokenHash} AND member_id = #{memberId} AND revoked_at IS NULL
            """)
    int revoke(@Param("memberId") long memberId, @Param("tokenHash") String tokenHash,
               @Param("revokedAt") LocalDateTime revokedAt);

    /** AI작성: 비밀번호 재설정 시 해당 회원의 폐기되지 않은 토큰을 모두 폐기한다. */
    @Update("""
            UPDATE refresh_token SET revoked_at = #{revokedAt}
            WHERE member_id = #{memberId} AND revoked_at IS NULL
            """)
    int revokeAllForMember(@Param("memberId") long memberId, @Param("revokedAt") LocalDateTime revokedAt);

    /** AI작성: 해시 처리된 새 refresh 토큰 정보를 저장한다. */
    @Insert("""
            INSERT INTO refresh_token (member_id, token_hash, expires_at, created_at)
            VALUES (#{memberId}, #{tokenHash}, #{expiresAt}, #{createdAt})
            """)
    int insert(@Param("memberId") long memberId, @Param("tokenHash") String tokenHash,
               @Param("expiresAt") LocalDateTime expiresAt, @Param("createdAt") LocalDateTime createdAt);

    /** AI작성: 재발급 검증에 필요한 refresh 토큰 상태 값이다. */
    record RefreshTokenSession(LocalDateTime expiresAt, LocalDateTime revokedAt) {}
}
