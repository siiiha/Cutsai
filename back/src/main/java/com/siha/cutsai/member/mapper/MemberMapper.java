package com.siha.cutsai.member.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** AI작성: 회원 계정과 내 정보 조회를 위한 DB 접근을 담당한다. */
@Mapper
public interface MemberMapper {
    /** AI작성: 이메일로 로그인에 필요한 회원 번호와 비밀번호 해시를 조회한다. */
    Optional<MemberCredentials> findCredentialsByEmail(@Param("email") String email);

    /** AI작성: 로그인 비밀번호 검증과 재설정이 겹치지 않도록 회원 행을 잠근다. */
    Optional<MemberCredentials> findCredentialsByEmailForUpdate(@Param("email") String email);

    /** AI작성: 토큰 재발급과 비밀번호 재설정이 겹치지 않도록 회원 행을 잠근다. */
    Optional<Long> lockById(@Param("memberId") long memberId);

    /** AI작성: 이메일 중복 여부를 확인한다. */
    boolean existsByEmail(@Param("email") String email);

    /** AI작성: 닉네임 중복 여부를 확인한다. */
    boolean existsByNickname(@Param("nickname") String nickname);

    /** AI작성: 지정한 회원의 닉네임을 변경한다. */
    int updateNickname(@Param("memberId") long memberId, @Param("nickname") String nickname,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /** AI작성: BCrypt 비밀번호 해시를 포함한 새 회원을 저장한다. */
    int insert(@Param("email") String email, @Param("passwordHash") String passwordHash,
               @Param("nickname") String nickname, @Param("createdAt") LocalDateTime createdAt);

    /** AI작성: 지정한 회원의 비밀번호 해시를 변경한다. */
    int updatePassword(@Param("memberId") long memberId, @Param("passwordHash") String passwordHash,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /** AI작성: 인증된 회원 번호로 내 정보 응답에 필요한 필드를 조회한다. */
    Optional<MemberRow> findById(@Param("memberId") long memberId);

    /** AI작성: 계정 변경 중 회원 정보가 동시에 변경되지 않도록 해당 행을 잠근다. */
    Optional<AccountCredentials> findAccountForUpdate(@Param("memberId") long memberId);

    /** AI작성: 회원의 탈퇴 요청 상태를 잠금 조회한다. */
    Optional<WithdrawalState> findWithdrawalForUpdate(@Param("memberId") long memberId);

    /** AI작성: 활성 회원에게 탈퇴 요청 시각을 기록한다. */
    int markWithdrawn(@Param("memberId") long memberId, @Param("withdrawnAt") LocalDateTime withdrawnAt);

    /** AI작성: 유예기간이 남은 탈퇴 계정을 활성 상태로 되돌린다. */
    int restoreWithinGrace(@Param("memberId") long memberId, @Param("cutoff") LocalDateTime cutoff);

    /** AI작성: 삭제 시점에 도달한 탈퇴 계정을 제한된 수만큼 잠금 조회한다. */
    List<WithdrawalState> findExpiredWithdrawalsForUpdate(@Param("cutoff") LocalDateTime cutoff,
                                                           @Param("limit") int limit);

    /** AI작성: 탈퇴하지 않은 회원인지 확인한다. */
    boolean isActive(@Param("memberId") long memberId);

    /** AI작성: 토큰 검증에 필요한 활성 상태와 버전을 조회한다. */
    Optional<TokenState> findTokenState(@Param("memberId") long memberId);

    /** AI작성: 탈퇴하는 회원이 소유한 회차 일정을 삭제한다. */
    int deleteEpisodeSchedules(@Param("memberId") long memberId);

    /** AI작성: 관련 데이터 정리 후 지정한 회원을 삭제한다. */
    int deleteMember(@Param("memberId") long memberId);

    /** AI작성: 로그인 검증 전용 회원 정보다. */
    record MemberCredentials(long memberId, String passwordHash) {}

    /** AI작성: 계정 변경 시 필요한 회원 정보와 비밀번호 해시다. */
    record AccountCredentials(String email, String nickname, String passwordHash) {}

    /** AI작성: 탈퇴 유예와 정리에 필요한 회원 상태다. */
    record WithdrawalState(long memberId, String email, LocalDateTime withdrawnAt) {}

    /** AI작성: 액세스 토큰과 비교할 회원 상태다. */
    record TokenState(boolean active, long tokenVersion) {}

    /** AI작성: 내 정보 응답 생성에 사용하는 회원 행이다. */
    record MemberRow(long memberId, String email, String nickname, LocalDateTime createdAt) {}
}
