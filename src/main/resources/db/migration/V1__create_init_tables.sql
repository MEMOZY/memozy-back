-- V1__init_schema.sql

-- 세션 문자셋
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- USERS
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user`
(
    `user_id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_role`         VARCHAR(10)  NOT NULL DEFAULT 'MEMBER',
    `profile_image_url` VARCHAR(2048),
    `username`          VARCHAR(100),
    `nickname`          VARCHAR(100),
    `phone_number`      VARCHAR(20),
    `email`             VARCHAR(2048),
    `friend_code`       VARCHAR(255) NOT NULL,
    `created_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_friend_code UNIQUE (`friend_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- FRIENDSHIP
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `friendship`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `sender_id`   BIGINT      NOT NULL,
    `receiver_id` BIGINT      NOT NULL,
    `status`      VARCHAR(32) NOT NULL, -- REQUESTED / ACCEPTED / REJECTED 등
    `created_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_friendship_sender (`sender_id`),
    KEY idx_friendship_receiver (`receiver_id`),
    CONSTRAINT fk_friendship_sender FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_receiver FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- MEMORY
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `memory`
(
    `memory_id`  BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`      VARCHAR(100) NOT NULL,
    `start_date` DATE,
    `end_date`   DATE,
    `category`   VARCHAR(30)  NOT NULL,
    `owner_id`   BIGINT       NOT NULL,
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_memory_owner (`owner_id`),
    CONSTRAINT fk_memory_owner FOREIGN KEY (`owner_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- MEMORY_ITEM
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `memory_item`
(
    `memory_item_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `memory_id`      BIGINT    NOT NULL,
    `file_key`       VARCHAR(2048),
    `content`        VARCHAR(1000),
    `sequence`       INT,
    `created_at`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_memory_item_memory (`memory_id`),
    CONSTRAINT fk_memory_item_memory FOREIGN KEY (`memory_id`) REFERENCES `memory` (`memory_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- MEMORY_SHARED
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `memory_shared`
(
    `id`         BIGINT PRIMARY KEY AUTO_INCREMENT,
    `memory_id`  BIGINT    NOT NULL,
    `user_id`    BIGINT    NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_memory_shared_memory (`memory_id`),
    KEY idx_memory_shared_user (`user_id`),
    CONSTRAINT fk_memory_shared_memory FOREIGN KEY (`memory_id`) REFERENCES `memory` (`memory_id`) ON DELETE CASCADE,
    CONSTRAINT fk_memory_shared_user FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- USER_POLICY_AGREEMENT
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_policy_agreement`
(
    `user_policy_agreement_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`                  BIGINT       NOT NULL,
    `policy_type`              VARCHAR(50)  NOT NULL,
    `version`                  VARCHAR(255) NOT NULL,
    `is_agree`                 TINYINT(1)   NOT NULL,
    `created_at`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_policy UNIQUE (`user_id`, `policy_type`, `version`),
    KEY idx_upa_user (`user_id`),
    CONSTRAINT fk_upa_user FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- SOCIAL_USER_INFO
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_user_info`
(
    `social_user_info_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`             BIGINT       NOT NULL,
    `social_type`         VARCHAR(10)  NOT NULL,
    `social_code`         VARCHAR(255) NOT NULL,
    `created_at`          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_social_code UNIQUE (`social_code`),
    KEY idx_sui_user (`user_id`),
    CONSTRAINT fk_sui_user FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;