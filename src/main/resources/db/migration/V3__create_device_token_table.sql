CREATE TABLE IF NOT EXISTS `device_tokens` (
    `id`         BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `user_id`    BIGINT NOT NULL,
    `expo_token` VARCHAR(255) NOT NULL,
    `platform`   VARCHAR(16)  NOT NULL,
    `is_valid`   BOOLEAN      NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT `uk_device_tokens_expo_token` UNIQUE (`expo_token`),

    CONSTRAINT `fk_device_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

CREATE INDEX `idx_device_tokens_user_valid`
    ON `device_tokens` (`user_id`, `is_valid`);