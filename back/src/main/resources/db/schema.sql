USE cutsai;

CREATE TABLE IF NOT EXISTS member (
                                      member_id BIGINT NOT NULL AUTO_INCREMENT,
                                      email VARCHAR(254) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email),
    UNIQUE KEY uk_member_nickname (nickname)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS episode_schedule (
                                                episode_id BIGINT NOT NULL AUTO_INCREMENT,
                                                member_id BIGINT NOT NULL,
                                                episode_label VARCHAR(100) NOT NULL,
    stage VARCHAR(30) NOT NULL,
    due_date DATE NOT NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (episode_id),
    KEY idx_episode_schedule_member_id (member_id),
    CONSTRAINT fk_episode_schedule_member_cascade
    /* AI작성: 회원 탈퇴 시 소유한 회차 일정도 함께 삭제한다. */
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS email_verification (
                                                  email VARCHAR(254) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    failed_attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
    resend_window_started_at DATETIME NULL,
    resend_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (email, purpose)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS login_rate_limit (
                                                limit_scope VARCHAR(30) NOT NULL,
    subject_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    failure_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (limit_scope, subject_hash)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_token (
                                             refresh_token_id BIGINT NOT NULL AUTO_INCREMENT,
                                             member_id BIGINT NOT NULL,
                                             token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    KEY idx_refresh_token_member_id (member_id),
    CONSTRAINT fk_refresh_token_member_cascade
    /* AI작성: 회원 탈퇴 시 해당 회원의 리프레시 토큰도 함께 삭제한다. */
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
