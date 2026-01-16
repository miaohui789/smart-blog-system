-- =============================================
-- 消息通知功能数据库表
-- 创建时间: 2026-01-13
-- =============================================

-- =============================================
-- 1. 系统通知表
-- =============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
    `type` VARCHAR(32) NOT NULL COMMENT '通知类型：LIKE_ARTICLE/FAVORITE_ARTICLE/COMMENT/REPLY/FOLLOW/SYSTEM',
    `title` VARCHAR(128) DEFAULT NULL COMMENT '通知标题',
    `content` VARCHAR(512) DEFAULT NULL COMMENT '通知内容',
    `sender_id` BIGINT DEFAULT NULL COMMENT '触发通知的用户ID（系统通知为空）',
    `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型：article/comment',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标ID（文章ID/评论ID等）',
    `extra_data` JSON DEFAULT NULL COMMENT '额外数据（JSON格式）',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`, `create_time` DESC) COMMENT '查询用户未读通知',
    KEY `idx_user_type` (`user_id`, `type`, `create_time` DESC) COMMENT '按类型查询通知',
    KEY `idx_create_time` (`create_time` DESC) COMMENT '按时间排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- =============================================
-- 2. 通知类型说明
-- =============================================
-- LIKE_ARTICLE    - 文章被点赞
-- FAVORITE_ARTICLE - 文章被收藏
-- COMMENT         - 文章被评论
-- REPLY           - 评论被回复
-- FOLLOW          - 被关注
-- MESSAGE         - 收到私信
-- SYSTEM          - 系统通知

-- =============================================
-- 3. 示例数据
-- =============================================
-- INSERT INTO `notification` (`user_id`, `type`, `title`, `content`, `sender_id`, `target_type`, `target_id`) VALUES
-- (1, 'LIKE_ARTICLE', '文章获得点赞', '用户xxx点赞了你的文章《xxx》', 2, 'article', 1),
-- (1, 'COMMENT', '文章收到评论', '用户xxx评论了你的文章《xxx》', 2, 'article', 1),
-- (1, 'REPLY', '评论收到回复', '用户xxx回复了你的评论', 2, 'comment', 1),
-- (1, 'FOLLOW', '新增粉丝', '用户xxx关注了你', 2, NULL, NULL);
