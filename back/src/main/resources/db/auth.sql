-- Apply after the account module creates member.
CREATE TABLE IF NOT EXISTS login_rate_limit (
    limit_scope VARCHAR(10) NOT NULL,
    subject_hash CHAR(64) NOT NULL,
    failure_count TINYINT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (limit_scope, subject_hash)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS refresh_token (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (refresh_token_id),
    UNIQUE (token_hash),
    FOREIGN KEY (member_id) REFERENCES member (member_id) ON DELETE CASCADE
) ENGINE=InnoDB;
