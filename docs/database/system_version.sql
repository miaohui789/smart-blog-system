-- 系统版本表
CREATE TABLE IF NOT EXISTS `system_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `version_number` VARCHAR(50) NOT NULL COMMENT '版本号（如：1.0.0）',
  `version_name` VARCHAR(100) NOT NULL COMMENT '版本名称',
  `release_date` DATE NOT NULL COMMENT '发布日期',
  `description` TEXT COMMENT '版本描述',
  `features` TEXT COMMENT '新增功能（JSON数组）',
  `improvements` TEXT COMMENT '优化改进（JSON数组）',
  `bug_fixes` TEXT COMMENT '修复问题（JSON数组）',
  `is_major` TINYINT(1) DEFAULT 0 COMMENT '是否为重大版本（0-否，1-是）',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-隐藏，1-显示）',
  `sort_order` INT DEFAULT 0 COMMENT '排序（数字越大越靠前）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_version_number` (`version_number`),
  KEY `idx_release_date` (`release_date`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统版本表';

-- 插入初始数据
INSERT INTO `system_version` (`version_number`, `version_name`, `release_date`, `description`, `features`, `improvements`, `bug_fixes`, `is_major`, `status`, `sort_order`) VALUES
('1.0.0', '正式版上线', '2026-01-22', 'MyBlog系统正式1.0版本上线，提供完整的博客功能', 
'["用户注册登录", "文章发布与管理", "评论互动系统", "标签分类功能", "VIP会员系统", "AI智能助手", "私信聊天功能", "消息通知系统"]',
'["响应式设计", "亮暗色主题切换", "代码高亮显示", "Markdown编辑器"]',
'[]',
1, 1, 100);
