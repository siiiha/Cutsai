package com.siha.cutsai.auth.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** AI작성: 이메일과 인증 목적별 코드 및 발송 제한 상태를 관리한다. */
@Mapper
public interface EmailVerificationMapper {
    /** AI작성: 최초 인증 행을 생성하고 기존 행은 변경하지 않는다. */
    @Insert("""
            INSERT INTO email_verification
                (email, purpose, code_hash, expires_at, resend_window_started_at, resend_count)
            VALUES (#{email}, #{purpose}, #{codeHash}, #{expiresAt}, #{windowStartedAt}, 0)
            ON DUPLICATE KEY UPDATE email = email
            """)
    int insertIfAbsent(@Param("email") String email, @Param("purpose") String purpose,
                       @Param("codeHash") String codeHash, @Param("expiresAt") LocalDateTime expiresAt,
                       @Param("windowStartedAt") LocalDateTime windowStartedAt);

    /** AI작성: 코드 검증과 발송 제한 갱신이 동시에 실행되지 않도록 행을 잠근다. */
    @Select("""
            SELECT code_hash, expires_at, verified_at, failed_attempts,
                   resend_window_started_at, resend_count
            FROM email_verification WHERE email = #{email} AND purpose = #{purpose} FOR UPDATE
            """)
    Optional<Verification> findForUpdate(@Param("email") String email, @Param("purpose") String purpose);

    /** AI작성: 새 코드와 발송 제한을 저장하고 이전 인증 및 실패 횟수를 초기화한다. */
    @Update("""
            UPDATE email_verification
            SET code_hash = #{codeHash}, expires_at = #{expiresAt}, verified_at = NULL,
                failed_attempts = 0, resend_window_started_at = #{windowStartedAt}, resend_count = #{resendCount}
            WHERE email = #{email} AND purpose = #{purpose}
            """)
    int updateCode(@Param("email") String email, @Param("purpose") String purpose,
                   @Param("codeHash") String codeHash, @Param("expiresAt") LocalDateTime expiresAt,
                   @Param("windowStartedAt") LocalDateTime windowStartedAt, @Param("resendCount") int resendCount);

    /** AI작성: 현재 코드의 오입력 횟수를 기록한다. */
    @Update("""
            UPDATE email_verification SET failed_attempts = #{failedAttempts}
            WHERE email = #{email} AND purpose = #{purpose}
            """)
    int updateFailedAttempts(@Param("email") String email, @Param("purpose") String purpose,
                             @Param("failedAttempts") int failedAttempts);

    /** AI작성: 현재 코드가 검증된 시각을 기록한다. */
    @Update("""
            UPDATE email_verification SET verified_at = #{verifiedAt}
            WHERE email = #{email} AND purpose = #{purpose}
            """)
    int markVerified(@Param("email") String email, @Param("purpose") String purpose,
                     @Param("verifiedAt") LocalDateTime verifiedAt);

    /** AI작성: 인증 사용을 완료해도 발송 제한 정보는 유지한다. */
    @Update("""
            UPDATE email_verification SET verified_at = NULL, expires_at = #{usedAt}
            WHERE email = #{email} AND purpose = #{purpose} AND verified_at IS NOT NULL
            """)
    int consume(@Param("email") String email, @Param("purpose") String purpose,
                @Param("usedAt") LocalDateTime usedAt);

    /** AI작성: 코드 및 발송 제한의 현재 상태다. */
    record Verification(String codeHash, LocalDateTime expiresAt, LocalDateTime verifiedAt,
                        int failedAttempts, LocalDateTime resendWindowStartedAt, int resendCount) {}
}
