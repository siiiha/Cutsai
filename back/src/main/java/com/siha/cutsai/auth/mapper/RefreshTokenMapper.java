package com.siha.cutsai.auth.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** AI작성: refresh_token 운영 데이터를 조회·폐기·저장한다. */
@Mapper
public interface RefreshTokenMapper {
    /** AI작성: 회원 탈퇴 시 해당 회원의 모든 리프레시 토큰 기록을 삭제한다. */
    int deleteByMemberId(@Param("memberId") long memberId);

    /** AI작성: 재발급 대상 토큰을 행 잠금으로 조회한다. */
    Optional<RefreshTokenSession> findForUpdate(@Param("memberId") long memberId,
                                                @Param("tokenHash") String tokenHash);

    /** AI작성: 사용된 refresh 토큰을 폐기 상태로 전환한다. */
    int revoke(@Param("memberId") long memberId, @Param("tokenHash") String tokenHash,
               @Param("revokedAt") LocalDateTime revokedAt);

    /** AI작성: 비밀번호 재설정 시 해당 회원의 폐기되지 않은 토큰을 모두 폐기한다. */
    int revokeAllForMember(@Param("memberId") long memberId, @Param("revokedAt") LocalDateTime revokedAt);

    /** AI작성: 해시 처리된 새 refresh 토큰 정보를 저장한다. */
    int insert(@Param("memberId") long memberId, @Param("tokenHash") String tokenHash,
               @Param("expiresAt") LocalDateTime expiresAt, @Param("createdAt") LocalDateTime createdAt);

    /** AI작성: 재발급 검증에 필요한 refresh 토큰 상태 값이다. */
    record RefreshTokenSession(LocalDateTime expiresAt, LocalDateTime revokedAt) {}
}
