-- =============================================
-- 系统版本管理功能完整安装脚本
-- 包含：表结构 + 菜单配置 + 初始数据
-- 执行方式：mysql -u root -p blog_db < system_version_complete.sql
-- =============================================

USE `blog_db`;

-- =============================================
-- 1. 创建系统版本表
-- =============================================
DROP TABLE IF EXISTS `system_version`;
CREATE TABLE `system_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `version_number` varchar(50) NOT NULL COMMENT '版本号',
  `version_name` varchar(100) NOT NULL COMMENT '版本名称',
  `release_date` date NOT NULL COMMENT '发布日期',
  `description` text COMMENT '版本描述',
  `features` text COMMENT '新增功能(JSON数组)',
  `improvements` text COMMENT '优化改进(JSON数组)',
  `bug_fixes` text COMMENT '修复问题(JSON数组)',
  `is_major` tinyint(1) DEFAULT '0' COMMENT '是否为重大版本(0-否 1-是)',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0-隐藏 1-显示)',
  `sort_order` int DEFAULT '0' COMMENT '排序(数字越大越靠前)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_version_number` (`version_number`),
  KEY `idx_release_date` (`release_date`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统版本表';

-- =============================================
-- 2. 插入版本管理菜单
-- =============================================

-- 检查菜单是否已存在，如果存在则更新，不存在则插入
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`)
VALUES (35, 30, '版本管理', 'C', '/system/version', 'System/Version', 'version:list', 'Histogram', 5, 1, 1)
ON DUPLICATE KEY UPDATE
  `menu_name` = '版本管理',
  `menu_type` = 'C',
  `path` = '/system/version',
  `component` = 'System/Version',
  `perms` = 'version:list',
  `icon` = 'Histogram',
  `sort` = 5,
  `visible` = 1,
  `status` = 1;

-- 插入按钮权限（使用424-427作为ID）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`)
VALUES 
(424, 35, '新增版本', 'F', NULL, NULL, 'version:add', NULL, 1, 1, 1),
(425, 35, '编辑版本', 'F', NULL, NULL, 'version:edit', NULL, 2, 1, 1),
(426, 35, '删除版本', 'F', NULL, NULL, 'version:delete', NULL, 3, 1, 1),
(427, 35, '更新状态', 'F', NULL, NULL, 'version:status', NULL, 4, 1, 1)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `perms` = VALUES(`perms`),
  `sort` = VALUES(`sort`);

-- =============================================
-- 3. 分配菜单权限给管理员角色
-- =============================================

-- 为管理员角色(role_id=1)分配所有权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 35),   -- 版本管理菜单
(1, 424),  -- 新增版本
(1, 425),  -- 编辑版本
(1, 426),  -- 删除版本
(1, 427);  -- 更新状态

-- 为编辑角色(role_id=2)分配部分权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 35),   -- 版本管理菜单
(2, 424),  -- 新增版本
(2, 425);  -- 编辑版本

-- =============================================
-- 4. 插入初始版本数据
-- =============================================

INSERT INTO `system_version` (
  `version_number`, 
  `version_name`, 
  `release_date`, 
  `description`, 
  `features`, 
  `improvements`, 
  `bug_fixes`, 
  `is_major`, 
  `status`, 
  `sort_order`
) VALUES (
  '1.0.0',
  '正式版上线',
  '2026-01-22',
  'MyBlog系统正式版发布，提供完整的博客管理和社交功能。',
  '["用户注册登录功能","文章发布与管理","评论互动系统","标签分类功能","VIP会员系统","AI智能助手","私信聊天功能","消息通知系统"]',
  '["优化页面加载速度","提升用户体验","完善响应式设计","增强系统安全性"]',
  '["修复登录状态丢失问题","修复评论显示异常","修复图片上传失败","修复深色主题样式问题"]',
  1,
  1,
  100
) ON DUPLICATE KEY UPDATE
  `version_name` = VALUES(`version_name`),
  `description` = VALUES(`description`);

-- =============================================
-- 执行完成提示
-- =============================================

SELECT '========================================' AS '';
SELECT '系统版本管理功能安装完成！' AS '提示';
SELECT '========================================' AS '';
SELECT '请执行以下操作：' AS '';
SELECT '1. 退出管理端登录' AS '';
SELECT '2. 重新登录管理员账号' AS '';
SELECT '3. 在"系统管理"下查看"版本管理"菜单' AS '';
SELECT '========================================' AS '';

-- 验证安装结果
SELECT '菜单配置验证：' AS '';
SELECT id, menu_name, menu_type, path, perms 
FROM sys_menu 
WHERE id IN (35, 424, 425, 426, 427)
ORDER BY id;

SELECT '角色权限验证：' AS '';
SELECT rm.role_id, r.role_name, COUNT(*) as menu_count
FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
WHERE rm.menu_id IN (35, 424, 425, 426, 427)
GROUP BY rm.role_id, r.role_name;

SELECT '版本数据验证：' AS '';
SELECT version_number, version_name, release_date, is_major, status
FROM system_version
ORDER BY sort_order DESC, release_date DESC;
