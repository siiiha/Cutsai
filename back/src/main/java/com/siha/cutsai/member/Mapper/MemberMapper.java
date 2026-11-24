package com.siha.cutsai.member.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** AI작성: 회원 계정과 내 정보 조회를 위한 DB 접근을 담당한다. */
@Mapper
public interface MemberMapper {
    /** AI작성: 이메일로 로그인에 필요한 회원 번호와 비밀번호 해시를 조회한다. */
    @Select("""
            SELECT member_id, password_hash FROM member
            WHERE BINARY LOWER(email) = #{email}
            """)
    Optional<MemberCredentials> findCredentialsByEmail(@Param("email") String email);

    /** AI작성: 이메일 중복 여부를 확인한다. */
    @Select("SELECT EXISTS(SELECT 1 FROM member WHERE BINARY LOWER(email) = #{email})")
    boolean existsByEmail(@Param("email") String email);

    /** AI작성: 닉네임 중복 여부를 확인한다. */
    @Select("SELECT EXISTS(SELECT 1 FROM member WHERE nickname = #{nickname})")
    boolean existsByNickname(@Param("nickname") String nickname);

    /** AI작성: BCrypt 비밀번호 해시를 포함한 새 회원을 저장한다. */
    @Insert("""
            INSERT INTO member (email, password_hash, nickname, created_at)
            VALUES (#{email}, #{passwordHash}, #{nickname}, #{createdAt})
            """)
    int insert(@Param("email") String email, @Param("passwordHash") String passwordHash,
               @Param("nickname") String nickname, @Param("createdAt") LocalDateTime createdAt);

    /** AI작성: 인증된 회원 번호로 내 정보 응답에 필요한 필드를 조회한다. */
    @Select("""
            SELECT member_id, email, nickname, created_at FROM member
            WHERE member_id = #{memberId}
            """)
    Optional<MemberRow> findById(@Param("memberId") long memberId);

    /** AI작성: 로그인 검증 전용 회원 정보다. */
    record MemberCredentials(long memberId, String passwordHash) {}

    /** AI작성: 내 정보 응답 생성에 사용하는 회원 행이다. */
    record MemberRow(long memberId, String email, String nickname, LocalDateTime createdAt) {}
}
