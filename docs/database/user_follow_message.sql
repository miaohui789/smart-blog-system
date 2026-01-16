-- =============================================
-- 用户关注与私信功能数据库扩展
-- 创建时间: 2026-01-13
-- =============================================

-- =============================================
-- 1. 用户关注表
-- =============================================
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '关注者ID（谁关注）',
    `follow_user_id` BIGINT NOT NULL COMMENT '被关注者ID（关注谁）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`) COMMENT '防止重复关注',
    KEY `idx_user_id` (`user_id`) COMMENT '查询我关注的人',
    KEY `idx_follow_user_id` (`follow_user_id`) COMMENT '查询关注我的人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- =============================================
-- 2. 私信会话表
-- =============================================
DROP TABLE IF EXISTS `message_conversation`;
CREATE TABLE `message_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` BIGINT NOT NULL COMMENT '用户1 ID（较小的ID）',
    `user2_id` BIGINT NOT NULL COMMENT '用户2 ID（较大的ID）',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `user1_unread` INT DEFAULT 0 COMMENT '用户1未读数',
    `user2_unread` INT DEFAULT 0 COMMENT '用户2未读数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`) COMMENT '两用户间唯一会话',
    KEY `idx_user1` (`user1_id`, `last_message_time` DESC),
    KEY `idx_user2` (`user2_id`, `last_message_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信会话表';

-- =============================================
-- 3. 私信消息表
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` TINYINT DEFAULT 1 COMMENT '消息类型：1文本 2图片 3系统消息',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`, `create_time` DESC),
    KEY `idx_receiver_unread` (`receiver_id`, `is_read`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- =============================================
-- 4. 用户表扩展字段（如果需要）
-- =============================================
-- 添加关注数和粉丝数字段（可选，用于快速查询）
ALTER TABLE `sys_user` 
ADD COLUMN `follow_count` INT DEFAULT 0 COMMENT '关注数' AFTER `vip_expire_time`,
ADD COLUMN `fans_count` INT DEFAULT 0 COMMENT '粉丝数' AFTER `follow_count`,
ADD COLUMN `article_count` INT DEFAULT 0 COMMENT '文章数' AFTER `fans_count`;

-- =============================================
-- 5. 初始化用户文章数统计
-- =============================================
UPDATE `sys_user` u 
SET `article_count` = (
    SELECT COUNT(*) FROM `blog_article` a 
    WHERE a.user_id = u.id AND a.status = 1 AND a.is_deleted = 0
);

-- =============================================
-- 6. 创建索引优化查询
-- =============================================
-- 文章表：按用户ID和发布时间查询
ALTER TABLE `blog_article` ADD INDEX `idx_user_publish` (`user_id`, `status`, `publish_time` DESC);

