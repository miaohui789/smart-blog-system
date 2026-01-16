-- =============================================
-- VIP 会员功能数据库扩展脚本
-- 执行前请确保已存在 blog_db 数据库
-- =============================================

USE `blog_db`;

-- 1. 用户表添加 VIP 相关字段
ALTER TABLE `sys_user` 
ADD COLUMN `is_vip` tinyint DEFAULT '0' COMMENT '是否VIP(0否 1是)' AFTER `is_deleted`,
ADD COLUMN `vip_level` tinyint DEFAULT '0' COMMENT 'VIP等级(0无 1普通 2高级 3超级)' AFTER `is_vip`,
ADD COLUMN `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间' AFTER `vip_level`;

-- 2. 创建 VIP 会员表
DROP TABLE IF EXISTS `vip_member`;
CREATE TABLE `vip_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT 'VIP等级(1普通 2高级 3超级)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `expire_time` datetime NOT NULL COMMENT '到期时间',
  `activation_key` varchar(64) DEFAULT NULL COMMENT '激活密钥',
  `status` tinyint DEFAULT '1' COMMENT '状态(0过期 1有效)',
  `heat_count_today` int DEFAULT '0' COMMENT '今日已用加热次数',
  `heat_date` date DEFAULT NULL COMMENT '加热次数重置日期',
  `download_count_today` int DEFAULT '0' COMMENT '今日已用下载次数',
  `download_date` date DEFAULT NULL COMMENT '下载次数重置日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP会员表';

-- 3. 创建 VIP 激活密钥表
DROP TABLE IF EXISTS `vip_activation_key`;
CREATE TABLE `vip_activation_key` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_code` varchar(64) NOT NULL COMMENT '密钥代码',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT 'VIP等级(1普通 2高级 3超级)',
  `duration_days` int NOT NULL DEFAULT '30' COMMENT '有效天数',
  `used_by` bigint DEFAULT NULL COMMENT '使用者用户ID',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `status` tinyint DEFAULT '1' COMMENT '状态(0已使用 1可用 2已禁用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime DEFAULT NULL COMMENT '密钥过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_key_code` (`key_code`),
  KEY `idx_status` (`status`),
  KEY `idx_used_by` (`used_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP激活密钥表';

-- 4. 创建文章加热记录表
DROP TABLE IF EXISTS `vip_article_heat`;
CREATE TABLE `vip_article_heat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `heat_value` int NOT NULL DEFAULT '10' COMMENT '加热值',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章加热记录表';

-- 5. 文章表添加热度字段
ALTER TABLE `blog_article` 
ADD COLUMN `heat_count` int DEFAULT '0' COMMENT '热度值' AFTER `favorite_count`;

-- 6. 插入 VIP 管理菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`) VALUES
(40, 0, '会员管理', 'M', '/vip', NULL, NULL, 'Medal', 5, 1, 1),
(41, 40, '会员列表', 'C', '/vip/member', 'Vip/Member', 'vip:member:list', 'User', 1, 1, 1),
(42, 40, '密钥管理', 'C', '/vip/key', 'Vip/Key', 'vip:key:list', 'Key', 2, 1, 1),
(43, 40, 'VIP统计', 'C', '/vip/statistics', 'Vip/Statistics', 'vip:statistics', 'DataAnalysis', 3, 1, 1),
-- 按钮权限
(411, 41, '编辑会员', 'F', NULL, NULL, 'vip:member:edit', NULL, 1, 1, 1),
(412, 41, '删除会员', 'F', NULL, NULL, 'vip:member:delete', NULL, 2, 1, 1),
(421, 42, '生成密钥', 'F', NULL, NULL, 'vip:key:generate', NULL, 1, 1, 1),
(422, 42, '禁用密钥', 'F', NULL, NULL, 'vip:key:disable', NULL, 2, 1, 1),
(423, 42, '删除密钥', 'F', NULL, NULL, 'vip:key:delete', NULL, 3, 1, 1);

-- 7. 为管理员角色分配 VIP 管理权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 40), (1, 41), (1, 42), (1, 43),
(1, 411), (1, 412), (1, 421), (1, 422), (1, 423);

-- 8. 插入示例激活密钥（用于测试）
INSERT INTO `vip_activation_key` (`key_code`, `level`, `duration_days`, `status`, `remark`, `expire_time`) VALUES
('VIP-TEST-0001-AAAA-BBBB', 1, 30, 1, '测试密钥-普通VIP-30天', DATE_ADD(NOW(), INTERVAL 1 YEAR)),
('VIP-TEST-0002-CCCC-DDDD', 2, 90, 1, '测试密钥-高级VIP-90天', DATE_ADD(NOW(), INTERVAL 1 YEAR)),
('VIP-TEST-0003-EEEE-FFFF', 3, 365, 1, '测试密钥-超级VIP-365天', DATE_ADD(NOW(), INTERVAL 1 YEAR));
