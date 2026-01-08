-- =============================================
-- 博客网站数据库初始化脚本
-- 数据库: blog_db
-- 字符集: utf8mb4
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `blog_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `blog_db`;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `intro` varchar(255) DEFAULT NULL COMMENT '个人简介',
  `website` varchar(255) DEFAULT NULL COMMENT '个人网站',
  `status` tinyint DEFAULT '1' COMMENT '状态(0禁用 1正常)',
  `login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除(0未删除 1已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) NOT NULL COMMENT '角色标识',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(0禁用 1正常)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';


-- =============================================
-- 3. 用户角色关联表
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 4. 菜单表
-- =============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) NOT NULL COMMENT '类型(M目录 C菜单 F按钮)',
  `path` varchar(200) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `visible` tinyint DEFAULT '1' COMMENT '是否可见(0隐藏 1显示)',
  `status` tinyint DEFAULT '1' COMMENT '状态(0禁用 1正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- =============================================
-- 5. 角色菜单关联表
-- =============================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- =============================================
-- 6. 文章表
-- =============================================
DROP TABLE IF EXISTS `blog_article`;
CREATE TABLE `blog_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) NOT NULL COMMENT '文章标题',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面图URL',
  `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
  `content` longtext COMMENT '文章内容(Markdown)',
  `content_html` longtext COMMENT '文章内容(HTML)',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏数',
  `is_top` tinyint DEFAULT '0' COMMENT '是否置顶(0否 1是)',
  `is_recommend` tinyint DEFAULT '0' COMMENT '是否推荐(0否 1是)',
  `status` tinyint DEFAULT '0' COMMENT '状态(0草稿 1发布 2下架)',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_is_top` (`is_top`),
  FULLTEXT KEY `ft_title_content` (`title`, `summary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';


-- =============================================
-- 7. 分类表
-- =============================================
DROP TABLE IF EXISTS `blog_category`;
CREATE TABLE `blog_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `article_count` int DEFAULT '0' COMMENT '文章数量',
  `status` tinyint DEFAULT '1' COMMENT '状态(0禁用 1正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- =============================================
-- 8. 标签表
-- =============================================
DROP TABLE IF EXISTS `blog_tag`;
CREATE TABLE `blog_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `color` varchar(20) DEFAULT '#a855f7' COMMENT '标签颜色',
  `article_count` int DEFAULT '0' COMMENT '文章数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- =============================================
-- 9. 文章标签关联表
-- =============================================
DROP TABLE IF EXISTS `blog_article_tag`;
CREATE TABLE `blog_article_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- =============================================
-- 10. 评论表
-- =============================================
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父评论ID',
  `reply_user_id` bigint DEFAULT NULL COMMENT '回复用户ID',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `status` tinyint DEFAULT '1' COMMENT '状态(0待审核 1通过 2拒绝)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 11. 文章点赞表
-- =============================================
DROP TABLE IF EXISTS `blog_article_like`;
CREATE TABLE `blog_article_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- =============================================
-- 12. 文章收藏表
-- =============================================
DROP TABLE IF EXISTS `blog_article_favorite`;
CREATE TABLE `blog_article_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';


-- =============================================
-- 13. 操作日志表
-- =============================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方式',
  `request_params` text COMMENT '请求参数',
  `response_data` text COMMENT '响应数据',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `ip_source` varchar(100) DEFAULT NULL COMMENT 'IP来源',
  `cost_time` bigint DEFAULT NULL COMMENT '耗时(ms)',
  `status` tinyint DEFAULT '1' COMMENT '状态(0失败 1成功)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 14. 登录日志表
-- =============================================
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
  `ip_source` varchar(100) DEFAULT NULL COMMENT 'IP来源',
  `browser` varchar(100) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) DEFAULT NULL COMMENT '操作系统',
  `status` tinyint DEFAULT '1' COMMENT '状态(0失败 1成功)',
  `message` varchar(255) DEFAULT NULL COMMENT '提示消息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- =============================================
-- 15. 系统配置表
-- =============================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_type` varchar(50) DEFAULT NULL COMMENT '配置类型',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';


-- =============================================
-- 初始数据插入
-- =============================================

-- 插入用户数据 (密码: admin123 -> BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `avatar`, `intro`, `status`) VALUES
(1, 'admin', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1WNrVUn/sLQ8fYYnQdXTVRqHDfEOi', '超级管理员', 'admin@blog.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '系统管理员', 1),
(2, 'editor', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1WNrVUn/sLQ8fYYnQdXTVRqHDfEOi', '内容编辑', 'editor@blog.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=editor', '负责内容编辑', 1),
(3, 'zhangsan', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1WNrVUn/sLQ8fYYnQdXTVRqHDfEOi', '张三', 'zhangsan@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan', '热爱技术的程序员', 1),
(4, 'lisi', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1WNrVUn/sLQ8fYYnQdXTVRqHDfEOi', '李四', 'lisi@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi', '前端开发工程师', 1),
(5, 'wangwu', '$2a$10$EqKcp1WFKVQISheBxkVJceXf1WNrVUn/sLQ8fYYnQdXTVRqHDfEOi', '王五', 'wangwu@example.com', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu', '后端开发工程师', 1);

-- 插入角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort`, `status`, `remark`) VALUES
(1, '超级管理员', 'admin', 1, 1, '拥有所有权限'),
(2, '内容编辑', 'editor', 2, 1, '负责内容管理'),
(3, '普通用户', 'user', 3, 1, '普通注册用户');

-- 插入用户角色关联数据
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 3);

-- 插入菜单数据
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`) VALUES
-- 仪表盘
(1, 0, '仪表盘', 'C', '/dashboard', 'Dashboard/index', 'dashboard:view', 'Dashboard', 1, 1, 1),
-- 内容管理
(10, 0, '内容管理', 'M', '/content', NULL, NULL, 'Document', 2, 1, 1),
(11, 10, '文章管理', 'C', '/content/article', 'Article/List', 'article:list', 'Notebook', 1, 1, 1),
(12, 10, '分类管理', 'C', '/content/category', 'Category/index', 'category:list', 'Folder', 2, 1, 1),
(13, 10, '标签管理', 'C', '/content/tag', 'Tag/index', 'tag:list', 'PriceTag', 3, 1, 1),
(14, 10, '评论管理', 'C', '/content/comment', 'Comment/index', 'comment:list', 'ChatDotRound', 4, 1, 1),
-- 用户管理
(20, 0, '用户管理', 'M', '/user', NULL, NULL, 'User', 3, 1, 1),
(21, 20, '用户列表', 'C', '/user/list', 'User/List', 'user:list', 'UserFilled', 1, 1, 1),
-- 系统管理
(30, 0, '系统管理', 'M', '/system', NULL, NULL, 'Setting', 4, 1, 1),
(31, 30, '角色管理', 'C', '/system/role', 'System/Role', 'role:list', 'Avatar', 1, 1, 1),
(32, 30, '菜单管理', 'C', '/system/menu', 'System/Menu', 'menu:list', 'Menu', 2, 1, 1),
(33, 30, '系统配置', 'C', '/system/config', 'System/Config', 'config:list', 'Tools', 3, 1, 1),
(34, 30, '操作日志', 'C', '/system/log', 'System/Log', 'log:list', 'Tickets', 4, 1, 1),
-- 按钮权限
(111, 11, '新增文章', 'F', NULL, NULL, 'article:add', NULL, 1, 1, 1),
(112, 11, '编辑文章', 'F', NULL, NULL, 'article:edit', NULL, 2, 1, 1),
(113, 11, '删除文章', 'F', NULL, NULL, 'article:delete', NULL, 3, 1, 1),
(121, 12, '新增分类', 'F', NULL, NULL, 'category:add', NULL, 1, 1, 1),
(122, 12, '编辑分类', 'F', NULL, NULL, 'category:edit', NULL, 2, 1, 1),
(123, 12, '删除分类', 'F', NULL, NULL, 'category:delete', NULL, 3, 1, 1);

-- 插入角色菜单关联数据 (管理员拥有所有权限)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(1, 20), (1, 21), (1, 30), (1, 31), (1, 32), (1, 33), (1, 34),
(1, 111), (1, 112), (1, 113), (1, 121), (1, 122), (1, 123),
-- 编辑角色权限
(2, 1), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14),
(2, 111), (2, 112), (2, 121), (2, 122);


-- 插入分类数据
INSERT INTO `blog_category` (`id`, `parent_id`, `name`, `icon`, `sort`, `article_count`, `status`) VALUES
(1, 0, '技术分享', 'Code', 1, 5, 1),
(2, 0, '生活随笔', 'Coffee', 2, 2, 1),
(3, 0, '学习笔记', 'Notebook', 3, 3, 1),
(4, 1, '前端开发', 'Monitor', 1, 3, 1),
(5, 1, '后端开发', 'Cpu', 2, 2, 1),
(6, 3, 'Java学习', 'Coffee', 1, 2, 1),
(7, 3, 'Vue学习', 'Connection', 2, 1, 1);

-- 插入标签数据
INSERT INTO `blog_tag` (`id`, `name`, `color`, `article_count`) VALUES
(1, 'Vue.js', '#42b883', 4),
(2, 'JavaScript', '#f7df1e', 3),
(3, 'Java', '#007396', 3),
(4, 'Spring Boot', '#6db33f', 2),
(5, 'MySQL', '#4479a1', 2),
(6, 'Redis', '#dc382d', 1),
(7, 'TypeScript', '#3178c6', 2),
(8, '前端', '#a855f7', 4),
(9, '后端', '#ec4899', 3),
(10, '教程', '#22c55e', 5);

-- 插入文章数据
INSERT INTO `blog_article` (`id`, `user_id`, `category_id`, `title`, `cover`, `summary`, `content`, `view_count`, `like_count`, `comment_count`, `favorite_count`, `is_top`, `is_recommend`, `status`, `publish_time`) VALUES
(1, 1, 4, 'Vue3 组合式API完全指南', 'https://picsum.photos/seed/vue3/800/400', 'Vue3的组合式API是一种全新的组织组件逻辑的方式，本文将详细介绍setup函数、响应式API、生命周期钩子等核心概念。', '# Vue3 组合式API完全指南\n\n## 前言\n\nVue3引入了组合式API（Composition API），这是一种全新的组织组件逻辑的方式。\n\n## setup函数\n\n```javascript\nimport { ref, reactive, computed } from ''vue''\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const state = reactive({ name: ''Vue3'' })\n    \n    const doubleCount = computed(() => count.value * 2)\n    \n    function increment() {\n      count.value++\n    }\n    \n    return { count, state, doubleCount, increment }\n  }\n}\n```\n\n## 响应式API\n\n### ref\n\n用于创建基本类型的响应式数据。\n\n### reactive\n\n用于创建对象类型的响应式数据。\n\n## 总结\n\n组合式API让我们能够更灵活地组织代码逻辑。', 1256, 89, 12, 45, 1, 1, 1, '2025-12-15 10:00:00'),

(2, 1, 5, 'Spring Boot 3.0 新特性详解', 'https://picsum.photos/seed/springboot/800/400', 'Spring Boot 3.0带来了许多激动人心的新特性，包括对Java 17的支持、GraalVM原生镜像、可观测性增强等。', '# Spring Boot 3.0 新特性详解\n\n## Java 17基线\n\nSpring Boot 3.0要求最低Java 17版本。\n\n## GraalVM原生镜像支持\n\n```xml\n<plugin>\n  <groupId>org.graalvm.buildtools</groupId>\n  <artifactId>native-maven-plugin</artifactId>\n</plugin>\n```\n\n## 可观测性增强\n\n新的Micrometer Observation API提供了统一的可观测性支持。\n\n## 总结\n\nSpring Boot 3.0是一个重要的里程碑版本。', 892, 67, 8, 32, 0, 1, 1, '2025-12-10 14:30:00'),

(3, 2, 4, 'Element Plus 深色主题定制实战', 'https://picsum.photos/seed/elementplus/800/400', '本文介绍如何为Element Plus定制深色主题，包括CSS变量覆盖、SCSS变量定制等方法。', '# Element Plus 深色主题定制实战\n\n## CSS变量覆盖\n\n```css\n:root {\n  --el-color-primary: #a855f7;\n  --el-bg-color: #1a1a1a;\n  --el-text-color-primary: #ffffff;\n}\n```\n\n## SCSS变量定制\n\n```scss\n@forward ''element-plus/theme-chalk/src/common/var.scss'' with (\n  $colors: (\n    ''primary'': (\n      ''base'': #a855f7,\n    ),\n  ),\n);\n```\n\n## 效果展示\n\n深色主题能够减少眼睛疲劳，提升夜间使用体验。', 654, 45, 6, 23, 0, 1, 1, '2025-12-08 09:15:00'),

(4, 3, 6, 'MyBatis Plus 快速入门教程', 'https://picsum.photos/seed/mybatisplus/800/400', 'MyBatis Plus是MyBatis的增强工具，本文将带你快速上手MyBatis Plus的基本使用。', '# MyBatis Plus 快速入门教程\n\n## 引入依赖\n\n```xml\n<dependency>\n  <groupId>com.baomidou</groupId>\n  <artifactId>mybatis-plus-boot-starter</artifactId>\n  <version>3.5.4</version>\n</dependency>\n```\n\n## 定义实体类\n\n```java\n@Data\n@TableName("user")\npublic class User {\n    @TableId(type = IdType.AUTO)\n    private Long id;\n    private String name;\n    private Integer age;\n}\n```\n\n## 定义Mapper\n\n```java\npublic interface UserMapper extends BaseMapper<User> {\n}\n```\n\n## CRUD操作\n\nMyBatis Plus提供了丰富的CRUD方法，无需编写SQL。', 523, 38, 5, 18, 0, 0, 1, '2025-12-05 16:45:00'),

(5, 4, 7, 'Pinia 状态管理最佳实践', 'https://picsum.photos/seed/pinia/800/400', 'Pinia是Vue3推荐的状态管理库，本文分享Pinia的最佳实践和使用技巧。', '# Pinia 状态管理最佳实践\n\n## 创建Store\n\n```javascript\nimport { defineStore } from ''pinia''\n\nexport const useUserStore = defineStore(''user'', {\n  state: () => ({\n    name: '''',\n    isLoggedIn: false\n  }),\n  getters: {\n    greeting: (state) => `Hello, ${state.name}`\n  },\n  actions: {\n    login(name) {\n      this.name = name\n      this.isLoggedIn = true\n    }\n  }\n})\n```\n\n## 在组件中使用\n\n```vue\n<script setup>\nimport { useUserStore } from ''@/stores/user''\n\nconst userStore = useUserStore()\n</script>\n```\n\n## 持久化\n\n使用pinia-plugin-persistedstate插件实现状态持久化。', 412, 29, 4, 15, 0, 0, 1, '2025-12-01 11:20:00');


-- 插入文章标签关联数据
INSERT INTO `blog_article_tag` (`article_id`, `tag_id`) VALUES
(1, 1), (1, 2), (1, 8), (1, 10),
(2, 3), (2, 4), (2, 9), (2, 10),
(3, 1), (3, 8), (3, 10),
(4, 3), (4, 5), (4, 9), (4, 10),
(5, 1), (5, 7), (5, 8), (5, 10);

-- 插入评论数据
INSERT INTO `blog_comment` (`id`, `article_id`, `user_id`, `parent_id`, `reply_user_id`, `content`, `like_count`, `status`) VALUES
(1, 1, 3, 0, NULL, '写得太好了！Vue3的组合式API确实比选项式API更灵活，感谢分享！', 15, 1),
(2, 1, 4, 0, NULL, '正在学习Vue3，这篇文章帮了大忙，收藏了！', 8, 1),
(3, 1, 5, 1, 3, '同意！特别是在处理复杂逻辑时，组合式API的优势更明显。', 5, 1),
(4, 2, 3, 0, NULL, 'Spring Boot 3.0的原生镜像支持太棒了，启动速度提升明显！', 12, 1),
(5, 2, 4, 4, 3, '请问原生镜像打包有什么需要注意的地方吗？', 3, 1),
(6, 2, 1, 5, 4, '需要注意反射的使用，可能需要额外配置hints。', 6, 1),
(7, 3, 5, 0, NULL, '深色主题看起来很舒服，已经按照教程配置好了！', 9, 1),
(8, 4, 4, 0, NULL, 'MyBatis Plus真的很方便，省去了很多重复代码。', 7, 1),
(9, 5, 3, 0, NULL, 'Pinia比Vuex简洁多了，推荐大家使用！', 11, 1),
(10, 5, 5, 9, 3, '确实，而且TypeScript支持也更好。', 4, 1);

-- 插入文章点赞数据
INSERT INTO `blog_article_like` (`article_id`, `user_id`) VALUES
(1, 3), (1, 4), (1, 5),
(2, 3), (2, 4),
(3, 3), (3, 5),
(4, 4), (4, 5),
(5, 3), (5, 4);

-- 插入文章收藏数据
INSERT INTO `blog_article_favorite` (`article_id`, `user_id`) VALUES
(1, 3), (1, 4), (1, 5),
(2, 3), (2, 5),
(3, 4),
(4, 3), (4, 5),
(5, 4);

-- 插入系统配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `remark`) VALUES
('site_name', '我的博客', 'site', '网站名称'),
('site_description', '一个专注于技术分享的博客网站', 'site', '网站描述'),
('site_keywords', '博客,技术,前端,后端,Vue,Java', 'site', '网站关键词'),
('site_logo', '/logo.png', 'site', '网站Logo'),
('site_favicon', '/favicon.ico', 'site', '网站图标'),
('site_footer', 'Copyright © 2025 My Blog. All rights reserved.', 'site', '页脚信息'),
('comment_audit', '0', 'comment', '评论是否需要审核(0否 1是)'),
('register_enabled', '1', 'user', '是否开放注册(0否 1是)'),
('default_avatar', 'https://api.dicebear.com/7.x/avataaars/svg', 'user', '默认头像'),
('upload_max_size', '10', 'upload', '上传文件最大大小(MB)'),
('upload_allowed_types', 'jpg,jpeg,png,gif,webp', 'upload', '允许上传的文件类型');

-- 插入登录日志数据
INSERT INTO `sys_login_log` (`user_id`, `username`, `ip`, `ip_source`, `browser`, `os`, `status`, `message`) VALUES
(1, 'admin', '127.0.0.1', '本地', 'Chrome 120', 'Windows 11', 1, '登录成功'),
(2, 'editor', '192.168.1.100', '内网', 'Firefox 121', 'macOS', 1, '登录成功'),
(3, 'zhangsan', '223.104.63.100', '北京市', 'Chrome 120', 'Windows 10', 1, '登录成功'),
(4, 'lisi', '116.25.146.200', '广东省深圳市', 'Safari 17', 'macOS', 1, '登录成功'),
(1, 'admin', '127.0.0.1', '本地', 'Chrome 120', 'Windows 11', 1, '登录成功');

-- 插入操作日志数据
INSERT INTO `sys_operation_log` (`user_id`, `module`, `description`, `request_url`, `request_method`, `ip`, `ip_source`, `cost_time`, `status`) VALUES
(1, '文章管理', '新增文章', '/api/admin/articles', 'POST', '127.0.0.1', '本地', 156, 1),
(1, '文章管理', '编辑文章', '/api/admin/articles/1', 'PUT', '127.0.0.1', '本地', 89, 1),
(1, '分类管理', '新增分类', '/api/admin/categories', 'POST', '127.0.0.1', '本地', 45, 1),
(2, '文章管理', '新增文章', '/api/admin/articles', 'POST', '192.168.1.100', '内网', 178, 1),
(1, '用户管理', '禁用用户', '/api/admin/users/6/status', 'PUT', '127.0.0.1', '本地', 67, 1);

-- =============================================
-- 初始化完成
-- =============================================
