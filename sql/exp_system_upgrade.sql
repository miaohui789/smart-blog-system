SET @db_name = DATABASE();

SET @has_current_exp = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'current_exp'
);
SET @sql_current_exp = IF(
  @has_current_exp = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `current_exp` int NULL DEFAULT 0 COMMENT ''当前等级经验'' AFTER `user_level`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_current_exp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_total_exp = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'total_exp'
);
SET @sql_total_exp = IF(
  @has_total_exp = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `total_exp` int NULL DEFAULT 0 COMMENT ''累计总经验'' AFTER `current_exp`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_total_exp;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_exp_update_time = (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'exp_update_time'
);
SET @sql_exp_update_time = IF(
  @has_exp_update_time = 0,
  'ALTER TABLE `sys_user` ADD COLUMN `exp_update_time` datetime NULL DEFAULT NULL COMMENT ''经验最近更新时间'' AFTER `total_exp`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_exp_update_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sys_user`
SET `current_exp` = 0
WHERE `current_exp` IS NULL OR `current_exp` < 0;

UPDATE `sys_user`
SET `total_exp` = 0
WHERE `total_exp` IS NULL OR `total_exp` < 0;

CREATE TABLE IF NOT EXISTS `user_exp_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `exp_change` int NOT NULL,
  `before_level` tinyint NULL DEFAULT NULL,
  `after_level` tinyint NULL DEFAULT NULL,
  `before_exp` int NULL DEFAULT NULL,
  `after_exp` int NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_biz`(`user_id`, `biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_user_time`(`user_id`, `create_time`) USING BTREE,
  INDEX `idx_biz_type_id`(`biz_type`, `biz_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `user_level_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level` tinyint NOT NULL,
  `need_exp` int NOT NULL,
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level`(`level`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

INSERT INTO `user_level_config` (`level`, `need_exp`, `title`, `status`)
SELECT
  seq.`level`,
  CASE
    WHEN seq.`level` BETWEEN 1 AND 10 THEN 80
    WHEN seq.`level` BETWEEN 11 AND 30 THEN 120
    WHEN seq.`level` BETWEEN 31 AND 60 THEN 180
    WHEN seq.`level` BETWEEN 61 AND 90 THEN 260
    ELSE 400
  END AS `need_exp`,
  CONCAT('Lv.', seq.`level`) AS `title`,
  1 AS `status`
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS `level`
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) tens
  WHERE ones.n + tens.n * 10 + 1 <= 100
) seq
ON DUPLICATE KEY UPDATE
  `need_exp` = VALUES(`need_exp`),
  `title` = VALUES(`title`),
  `status` = VALUES(`status`);
