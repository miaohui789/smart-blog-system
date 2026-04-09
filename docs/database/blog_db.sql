/*
 Navicat Premium Data Transfer

 Source Server         : 3306
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3306
 Source Schema         : blog_db

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 16/03/2026 21:37:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_config`;
CREATE TABLE `ai_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'deepseek' COMMENT 'AI服务商: deepseek, openai, zhipu, qianfan, dashscope',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'deepseek-chat' COMMENT '模型名称',
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型显示名称',
  `api_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API密钥(加密存储)',
  `base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'API基础地址',
  `max_tokens` int NULL DEFAULT 2000 COMMENT '最大token数',
  `temperature` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '温度参数(0-1)',
  `system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '系统提示词',
  `enabled` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
  `daily_limit` int NULL DEFAULT 100 COMMENT '每用户每日请求限制',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否为默认模型（1=是 0=否）',
  `use_for_chat` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否用于AI对话',
  `use_for_study_score` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否用于学习模块AI评分',
  `is_default_study_score` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为学习模块AI评分默认模型',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `support_thinking` tinyint(1) NULL DEFAULT 0,
  `logo_id` bigint NULL DEFAULT NULL COMMENT '关联的Logo ID',
  `score_system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学习模块AI评分系统提示词',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `config_id` bigint NULL DEFAULT NULL COMMENT '使用的AI配置ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '新对话' COMMENT '会话标题',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI对话会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_logo
-- ----------------------------
DROP TABLE IF EXISTS `ai_logo`;
CREATE TABLE `ai_logo`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '服务商标识（deepseek, openai, zhipu 等）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Logo名称/备注',
  `logo_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'url' COMMENT 'Logo类型：url-网络地址, upload-上传图片',
  `logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Logo地址（网络URL或上传路径）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_provider`(`provider` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI服务商Logo表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: user, assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `tokens` int NULL DEFAULT 0 COMMENT '消耗token数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `thinking_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 176 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI对话消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_usage
-- ----------------------------
DROP TABLE IF EXISTS `ai_usage`;
CREATE TABLE `ai_usage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `date` date NOT NULL COMMENT '日期',
  `request_count` int NULL DEFAULT 0 COMMENT '请求次数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '消耗token总数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `date` ASC) USING BTREE,
  INDEX `idx_date`(`date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI使用统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_user_model
-- ----------------------------
DROP TABLE IF EXISTS `ai_user_model`;
CREATE TABLE `ai_user_model`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `config_id` bigint NOT NULL COMMENT 'AI配置ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_config_id`(`config_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户AI模型偏好表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_article
-- ----------------------------
DROP TABLE IF EXISTS `blog_article`;
CREATE TABLE `blog_article`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容(Markdown)',
  `content_html` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章内容(HTML)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  `favorite_count` int NULL DEFAULT 0 COMMENT '收藏数',
  `heat_count` int NULL DEFAULT 0 COMMENT '热度值',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶(0否 1是)',
  `is_recommend` tinyint NULL DEFAULT 0 COMMENT '是否推荐(0否 1是)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态(0草稿 1发布 2下架)',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_user_publish`(`user_id` ASC, `status` ASC, `publish_time` DESC) USING BTREE,
  FULLTEXT INDEX `ft_title_content`(`title`, `summary`)
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_article_favorite
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_favorite`;
CREATE TABLE `blog_article_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_user`(`article_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_article_like
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_like`;
CREATE TABLE `blog_article_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_user`(`article_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_article_tag
-- ----------------------------
DROP TABLE IF EXISTS `blog_article_tag`;
CREATE TABLE `blog_article_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_tag`(`article_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_category
-- ----------------------------
DROP TABLE IF EXISTS `blog_category`;
CREATE TABLE `blog_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `article_count` int NULL DEFAULT 0 COMMENT '文章数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_comment
-- ----------------------------
DROP TABLE IF EXISTS `blog_comment`;
CREATE TABLE `blog_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父评论ID',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复用户ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0待审核 1通过 2拒绝)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for blog_tag
-- ----------------------------
DROP TABLE IF EXISTS `blog_tag`;
CREATE TABLE `blog_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#a855f7' COMMENT '标签颜色',
  `article_count` int NULL DEFAULT 0 COMMENT '文章数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `type` tinyint NULL DEFAULT 1 COMMENT '消息类型：1文本 2图片 3系统消息',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `is_withdrawn` tinyint(1) NULL DEFAULT 0 COMMENT '是否已撤回：0否 1是',
  `sender_deleted` tinyint NULL DEFAULT 0 COMMENT '发送者是否删除：0否 1是',
  `receiver_deleted` tinyint NULL DEFAULT 0 COMMENT '接收者是否删除：0否 1是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation`(`conversation_id` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_receiver_unread`(`receiver_id` ASC, `is_read` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_sender_deleted`(`sender_id` ASC, `sender_deleted` ASC) USING BTREE,
  INDEX `idx_receiver_deleted`(`receiver_id` ASC, `receiver_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message_conversation
-- ----------------------------
DROP TABLE IF EXISTS `message_conversation`;
CREATE TABLE `message_conversation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` bigint NOT NULL COMMENT '用户1 ID（较小的ID）',
  `user2_id` bigint NOT NULL COMMENT '用户2 ID（较大的ID）',
  `last_message_id` bigint NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `user1_unread` int NULL DEFAULT 0 COMMENT '用户1未读数',
  `user2_unread` int NULL DEFAULT 0 COMMENT '用户2未读数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users`(`user1_id` ASC, `user2_id` ASC) USING BTREE COMMENT '两用户间唯一会话',
  INDEX `idx_user1`(`user1_id` ASC, `last_message_time` DESC) USING BTREE,
  INDEX `idx_user2`(`user2_id` ASC, `last_message_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：LIKE_ARTICLE/FAVORITE_ARTICLE/COMMENT/REPLY/FOLLOW/SYSTEM',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '触发通知的用户ID（系统通知为空）',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型：article/comment',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID（文章ID/评论ID等）',
  `extra_data` json NULL COMMENT '额外数据（JSON格式）',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_read`(`user_id` ASC, `is_read` ASC, `create_time` DESC) USING BTREE COMMENT '查询用户未读通知',
  INDEX `idx_user_type`(`user_id` ASC, `type` ASC, `create_time` DESC) USING BTREE COMMENT '按类型查询通知',
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE COMMENT '按时间排序'
) ENGINE = InnoDB AUTO_INCREMENT = 72 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_ai_score_record
-- ----------------------------
DROP TABLE IF EXISTS `study_ai_score_record`;
CREATE TABLE `study_ai_score_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `score_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评分记录编号',
  `answer_id` bigint NOT NULL COMMENT '答题记录ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_item_id` bigint NOT NULL COMMENT '任务明细ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `ai_config_id` bigint NULL DEFAULT NULL COMMENT 'AI配置ID，可关联现有 ai_config.id',
  `model_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型供应商',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示词版本',
  `full_score` decimal(6, 2) NOT NULL DEFAULT 100.00 COMMENT '本次评分满分',
  `ai_score` decimal(6, 2) NULL DEFAULT NULL COMMENT 'AI评分',
  `result_level` tinyint NULL DEFAULT NULL COMMENT '结果等级：1优秀 2良好 3及格 4待提升',
  `dimension_scores` json NULL COMMENT '维度得分JSON，例如准确性、完整性、表达性',
  `keyword_hit_json` json NULL COMMENT '关键词命中JSON',
  `strengths_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '优点分析',
  `weaknesses_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '不足分析',
  `suggestion_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '改进建议',
  `request_tokens` int NOT NULL DEFAULT 0 COMMENT '请求Token',
  `response_tokens` int NOT NULL DEFAULT 0 COMMENT '响应Token',
  `total_tokens` int NOT NULL DEFAULT 0 COMMENT '总Token',
  `score_status` tinyint NOT NULL DEFAULT 0 COMMENT '评分状态：0待处理 1成功 2失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `request_time` datetime NULL DEFAULT NULL COMMENT '请求时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `duration_ms` int NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  `raw_response` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型原始响应',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_score_no`(`score_no` ASC) USING BTREE,
  INDEX `idx_answer_id`(`answer_id` ASC, `score_status` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_question_time`(`question_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI评分记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_answer_record
-- ----------------------------
DROP TABLE IF EXISTS `study_answer_record`;
CREATE TABLE `study_answer_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `answer_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '答题记录编号',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_item_id` bigint NOT NULL COMMENT '任务明细ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `answer_round` int NOT NULL DEFAULT 1 COMMENT '第几次作答',
  `answer_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户答案',
  `char_count` int NOT NULL DEFAULT 0 COMMENT '答案字符数',
  `word_count` int NOT NULL DEFAULT 0 COMMENT '答案词数',
  `answer_language` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'zh-CN' COMMENT '答案语言',
  `answer_source` tinyint NOT NULL DEFAULT 1 COMMENT '答案来源：1手输 2语音转写 3粘贴导入',
  `answer_status` tinyint NOT NULL DEFAULT 0 COMMENT '答题状态：0草稿 1已提交 2已评分 3已放弃',
  `self_assessment_result` tinyint NULL DEFAULT NULL COMMENT '自评结果：1记得 2模糊 3忘记',
  `self_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '自评分',
  `self_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '自评备注',
  `standard_answer_viewed` tinyint NOT NULL DEFAULT 0 COMMENT '提交前是否查看过标准答案：0否 1是',
  `standard_answer_view_time` datetime NULL DEFAULT NULL COMMENT '查看标准答案时间',
  `ai_score_status` tinyint NOT NULL DEFAULT 0 COMMENT 'AI评分状态：0未触发 1评分中 2成功 3失败',
  `final_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '最终得分',
  `is_best_answer` tinyint NOT NULL DEFAULT 0 COMMENT '是否最佳记录：0否 1是',
  `submitted_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_answer_no`(`answer_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_task_item_round`(`task_item_id` ASC, `answer_round` ASC) USING BTREE,
  INDEX `idx_task_user`(`task_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_question_user`(`question_id` ASC, `user_id` ASC, `submitted_time` DESC) USING BTREE,
  INDEX `idx_ai_score_status`(`ai_score_status` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_category
-- ----------------------------
DROP TABLE IF EXISTS `study_category`;
CREATE TABLE `study_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级分类ID，0表示顶级分类',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类编码，系统内唯一',
  `category_level` tinyint NOT NULL DEFAULT 1 COMMENT '分类层级：1一级 2二级 3三级',
  `category_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类路径，例如 /1/11/111',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '来源类型：1手工创建 2Markdown导入 3脚本导入',
  `source_file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源文件名',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `question_count` int NOT NULL DEFAULT 0 COMMENT '题目数量',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_parent_sort`(`parent_id` ASC, `sort_order` DESC) USING BTREE,
  INDEX `idx_status_deleted`(`status` ASC, `is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_check_task
-- ----------------------------
DROP TABLE IF EXISTS `study_check_task`;
CREATE TABLE `study_check_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `task_source` tinyint NOT NULL DEFAULT 1 COMMENT '任务来源：1用户手动发起 2系统推荐 3后台指定',
  `check_mode` tinyint NOT NULL DEFAULT 1 COMMENT '抽查模式：1随机 2分类专项 3收藏题 4薄弱题 5错题本 6混合',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `filter_json` json NULL COMMENT '筛选条件JSON，例如标签、难度、范围',
  `question_count` int NOT NULL DEFAULT 0 COMMENT '题目总数',
  `answered_count` int NOT NULL DEFAULT 0 COMMENT '已答题数',
  `scored_count` int NOT NULL DEFAULT 0 COMMENT '已评分题数',
  `need_ai_score` tinyint NOT NULL DEFAULT 1 COMMENT '是否需要AI评分：0否 1是',
  `allow_self_assessment` tinyint NOT NULL DEFAULT 1 COMMENT '是否允许自评：0否 1是',
  `show_standard_answer` tinyint NOT NULL DEFAULT 1 COMMENT '是否允许查看标准答案：0否 1是',
  `scoring_mode` tinyint NOT NULL DEFAULT 3 COMMENT '评分模式：1仅自评 2仅AI 3AI+自评混合',
  `score_rule_json` json NULL COMMENT '本次评分规则JSON快照',
  `full_score` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '总满分',
  `self_score` decimal(8, 2) NULL DEFAULT NULL COMMENT '自评总分',
  `ai_score` decimal(8, 2) NULL DEFAULT NULL COMMENT 'AI总分',
  `final_score` decimal(8, 2) NULL DEFAULT NULL COMMENT '最终总分',
  `remember_count` int NOT NULL DEFAULT 0 COMMENT '记得题数',
  `fuzzy_count` int NOT NULL DEFAULT 0 COMMENT '模糊题数',
  `forget_count` int NOT NULL DEFAULT 0 COMMENT '忘记题数',
  `excellent_count` int NOT NULL DEFAULT 0 COMMENT '优秀题数',
  `pass_count` int NOT NULL DEFAULT 0 COMMENT '及格题数',
  `failed_count` int NOT NULL DEFAULT 0 COMMENT '不及格题数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0待开始 1进行中 2已完成 3已终止 4已过期',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '交卷时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `duration_seconds` int NOT NULL DEFAULT 0 COMMENT '耗时秒数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_no`(`task_no` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_user_mode`(`user_id` ASC, `check_mode` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抽查任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_check_task_item
-- ----------------------------
DROP TABLE IF EXISTS `study_check_task_item`;
CREATE TABLE `study_check_task_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_version_no` int NULL DEFAULT NULL COMMENT '题目版本号',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '题目顺序',
  `question_title_snapshot` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题快照',
  `question_stem_snapshot` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题干快照',
  `standard_answer_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '标准答案快照',
  `score_full_mark` decimal(6, 2) NOT NULL DEFAULT 100.00 COMMENT '本题满分快照',
  `score_pass_mark` decimal(6, 2) NOT NULL DEFAULT 60.00 COMMENT '本题及格分快照',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待答题 1答题中 2已提交 3已评分 4已跳过',
  `answer_count` int NOT NULL DEFAULT 0 COMMENT '答题次数',
  `view_answer_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否看过标准答案：0否 1是',
  `view_answer_time` datetime NULL DEFAULT NULL COMMENT '查看标准答案时间',
  `self_assessment_result` tinyint NULL DEFAULT NULL COMMENT '自评结果：1记得 2模糊 3忘记',
  `self_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '自评分',
  `ai_score` decimal(6, 2) NULL DEFAULT NULL COMMENT 'AI评分',
  `final_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '最终得分',
  `last_answer_id` bigint NULL DEFAULT NULL COMMENT '最近答题记录ID',
  `last_ai_score_record_id` bigint NULL DEFAULT NULL COMMENT '最近AI评分记录ID',
  `answer_duration_seconds` int NOT NULL DEFAULT 0 COMMENT '答题耗时秒数',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_question`(`task_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_task_sort`(`task_id` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_task_status`(`task_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 76 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '抽查任务明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_import_batch
-- ----------------------------
DROP TABLE IF EXISTS `study_import_batch`;
CREATE TABLE `study_import_batch`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '来源类型：1Markdown 2Excel 3接口 4脚本',
  `source_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源名称',
  `source_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源路径',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总记录数',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '失败数',
  `duplicate_count` int NOT NULL DEFAULT 0 COMMENT '重复数',
  `batch_status` tinyint NOT NULL DEFAULT 0 COMMENT '批次状态：0处理中 1成功 2部分成功 3失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批次错误信息',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_no`(`batch_no` ASC) USING BTREE,
  INDEX `idx_status`(`batch_status` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_operator`(`operator_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题库导入批次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_import_batch_detail
-- ----------------------------
DROP TABLE IF EXISTS `study_import_batch_detail`;
CREATE TABLE `study_import_batch_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `source_line_no` int NULL DEFAULT NULL COMMENT '源文件行号',
  `source_section` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '源章节名称',
  `source_question_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '源题号',
  `source_title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '源标题',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '识别出的分类名',
  `question_id` bigint NULL DEFAULT NULL COMMENT '导入后题目ID',
  `process_status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1成功 2失败 3重复跳过',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `raw_payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '原始数据内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_batch_status`(`batch_id` ASC, `process_status` ASC) USING BTREE,
  INDEX `idx_question`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题库导入批次明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_question
-- ----------------------------
DROP TABLE IF EXISTS `study_question`;
CREATE TABLE `study_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '所属分类ID',
  `question_no` int NULL DEFAULT NULL COMMENT '题号',
  `question_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目编码，系统内唯一',
  `question_type` tinyint NOT NULL DEFAULT 1 COMMENT '题目类型：1问答题 2概念题 3原理题 4场景题 5方案题',
  `title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题',
  `question_stem` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题干补充内容',
  `standard_answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准答案',
  `answer_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '答案摘要',
  `keywords` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关键字，英文逗号分隔',
  `difficulty` tinyint NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3较难 4困难 5专家',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '来源类型：1手工创建 2Markdown导入 3脚本导入 4外部同步',
  `source_file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源文件名',
  `source_section` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源章节',
  `source_ref_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '来源原始编号',
  `import_batch_id` bigint NULL DEFAULT NULL COMMENT '导入批次ID',
  `estimated_minutes` smallint NOT NULL DEFAULT 5 COMMENT '预估答题时长，单位分钟',
  `answer_word_count` int NOT NULL DEFAULT 0 COMMENT '标准答案字数',
  `score_full_mark` decimal(6, 2) NOT NULL DEFAULT 100.00 COMMENT '满分分值',
  `score_pass_mark` decimal(6, 2) NOT NULL DEFAULT 60.00 COMMENT '及格分值',
  `ai_score_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否开启 AI 打分：0否 1是',
  `self_assessment_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否开启自评：0否 1是',
  `ai_score_prompt_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AI评分提示词版本',
  `score_rubric_json` json NULL COMMENT '评分规则JSON',
  `review_status` tinyint NOT NULL DEFAULT 1 COMMENT '审核状态：0草稿 1待审核 2已发布 3已下线',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '当前版本号',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '查看次数',
  `study_count` int NOT NULL DEFAULT 0 COMMENT '学习次数',
  `check_count` int NOT NULL DEFAULT 0 COMMENT '被抽查次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question_code`(`question_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_category_question_no`(`category_id` ASC, `question_no` ASC) USING BTREE,
  INDEX `idx_category_status`(`category_id` ASC, `status` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_review_status`(`review_status` ASC, `publish_time` DESC) USING BTREE,
  INDEX `idx_import_batch`(`import_batch_id` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 203 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习题目主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_question_tag
-- ----------------------------
DROP TABLE IF EXISTS `study_question_tag`;
CREATE TABLE `study_question_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question_tag`(`question_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_tag_id`(`tag_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_question_version
-- ----------------------------
DROP TABLE IF EXISTS `study_question_version`;
CREATE TABLE `study_question_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `title` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题快照',
  `question_stem` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题干快照',
  `standard_answer` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准答案快照',
  `answer_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '答案摘要快照',
  `keywords` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关键词快照',
  `difficulty` tinyint NOT NULL DEFAULT 2 COMMENT '难度快照',
  `score_full_mark` decimal(6, 2) NOT NULL DEFAULT 100.00 COMMENT '满分快照',
  `score_pass_mark` decimal(6, 2) NOT NULL DEFAULT 60.00 COMMENT '及格分快照',
  `score_rubric_json` json NULL COMMENT '评分规则快照JSON',
  `change_type` tinyint NOT NULL DEFAULT 1 COMMENT '变更类型：1创建 2编辑 3导入覆盖 4发布 5下线',
  `change_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更原因',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question_version`(`question_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC, `create_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_tag
-- ----------------------------
DROP TABLE IF EXISTS `study_tag`;
CREATE TABLE `study_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `tag_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签编码，系统内唯一',
  `tag_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签颜色',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签描述',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  UNIQUE INDEX `uk_tag_code`(`tag_code` ASC) USING BTREE,
  INDEX `idx_status_deleted`(`status` ASC, `is_deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_user_question_note
-- ----------------------------
DROP TABLE IF EXISTS `study_user_question_note`;
CREATE TABLE `study_user_question_note`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `note_type` tinyint NOT NULL DEFAULT 1 COMMENT '笔记类型：1普通笔记 2复盘笔记 3错因笔记 4面试模板',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记内容',
  `is_pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_question`(`user_id` ASC, `question_id` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_question_type`(`question_id` ASC, `note_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户题目笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for study_user_question_progress
-- ----------------------------
DROP TABLE IF EXISTS `study_user_question_progress`;
CREATE TABLE `study_user_question_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，对应 sys_user.id',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `study_status` tinyint NOT NULL DEFAULT 0 COMMENT '学习状态：0未开始 1学习中 2已掌握 3待复习',
  `mastery_level` tinyint NOT NULL DEFAULT 0 COMMENT '掌握度：0未知 1较弱 2一般 3良好 4熟练 5完全掌握',
  `review_priority` tinyint NOT NULL DEFAULT 3 COMMENT '复习优先级：1低 2较低 3中 4较高 5高',
  `study_count` int NOT NULL DEFAULT 0 COMMENT '累计学习次数',
  `check_count` int NOT NULL DEFAULT 0 COMMENT '累计抽查次数',
  `remember_count` int NOT NULL DEFAULT 0 COMMENT '自评记得次数',
  `fuzzy_count` int NOT NULL DEFAULT 0 COMMENT '自评模糊次数',
  `forget_count` int NOT NULL DEFAULT 0 COMMENT '自评忘记次数',
  `wrong_count` int NOT NULL DEFAULT 0 COMMENT '判定为低分或忘记的次数',
  `last_result` tinyint NULL DEFAULT NULL COMMENT '最近结果：1记得 2模糊 3忘记',
  `last_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '最近得分',
  `best_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '最高得分',
  `avg_score` decimal(6, 2) NULL DEFAULT NULL COMMENT '平均得分',
  `last_answer_id` bigint NULL DEFAULT NULL COMMENT '最近一次答题记录ID',
  `last_ai_score_record_id` bigint NULL DEFAULT NULL COMMENT '最近一次AI评分记录ID',
  `is_favorite` tinyint NOT NULL DEFAULT 0 COMMENT '是否收藏：0否 1是',
  `is_wrong_question` tinyint NOT NULL DEFAULT 0 COMMENT '是否在错题本：0否 1是',
  `note_count` int NOT NULL DEFAULT 0 COMMENT '笔记数量',
  `first_study_time` datetime NULL DEFAULT NULL COMMENT '首次学习时间',
  `last_study_time` datetime NULL DEFAULT NULL COMMENT '最近学习时间',
  `last_check_time` datetime NULL DEFAULT NULL COMMENT '最近抽查时间',
  `next_review_time` datetime NULL DEFAULT NULL COMMENT '下次建议复习时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `study_status` ASC) USING BTREE,
  INDEX `idx_user_mastery`(`user_id` ASC, `mastery_level` ASC) USING BTREE,
  INDEX `idx_user_favorite`(`user_id` ASC, `is_favorite` ASC) USING BTREE,
  INDEX `idx_user_wrong`(`user_id` ASC, `is_wrong_question` ASC) USING BTREE,
  INDEX `idx_next_review`(`user_id` ASC, `next_review_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户题目学习进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置类型',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录IP',
  `ip_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP来源',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0失败 1成功)',
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示消息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型(M目录 C菜单 F按钮)',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见(0隐藏 1显示)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 427 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作模块',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应数据',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP',
  `ip_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP来源',
  `cost_time` bigint NULL DEFAULT NULL COMMENT '耗时(ms)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0失败 1成功)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色标识',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE,
  INDEX `idx_menu_id`(`menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `intro` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `website` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人网站',
  `theme_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'dark' COMMENT '主题模式: dark/light',
  `dark_skin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'default' COMMENT '暗色主题皮肤',
  `light_skin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'default' COMMENT '亮色主题皮肤',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0禁用 1正常)',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除(0未删除 1已删除)',
  `is_vip` tinyint NULL DEFAULT 0 COMMENT '是否VIP(0否 1是)',
  `vip_level` tinyint NULL DEFAULT 0 COMMENT 'VIP等级(0无 1普通 2高级 3超级)',
  `vip_expire_time` datetime NULL DEFAULT NULL COMMENT 'VIP到期时间',
  `follow_count` int NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` int NULL DEFAULT 0 COMMENT '粉丝数',
  `article_count` int NULL DEFAULT 0 COMMENT '文章数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_version
-- ----------------------------
DROP TABLE IF EXISTS `system_version`;
CREATE TABLE `system_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `version_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本号（如：1.0.0）',
  `version_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本名称',
  `release_date` date NOT NULL COMMENT '发布日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '版本描述',
  `features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '新增功能（JSON数组）',
  `improvements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '优化改进（JSON数组）',
  `bug_fixes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '修复问题（JSON数组）',
  `is_major` tinyint(1) NULL DEFAULT 0 COMMENT '是否为重大版本（0-否，1-是）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态（0-隐藏，1-显示）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序（数字越大越靠前）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_version_number`(`version_number` ASC) USING BTREE,
  INDEX `idx_release_date`(`release_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关注者ID（谁关注）',
  `follow_user_id` bigint NOT NULL COMMENT '被关注者ID（关注谁）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id` ASC, `follow_user_id` ASC) USING BTREE COMMENT '防止重复关注',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '查询我关注的人',
  INDEX `idx_follow_user_id`(`follow_user_id` ASC) USING BTREE COMMENT '查询关注我的人'
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户关注表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vip_activation_key
-- ----------------------------
DROP TABLE IF EXISTS `vip_activation_key`;
CREATE TABLE `vip_activation_key`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `key_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密钥代码',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT 'VIP等级(1普通 2高级 3超级)',
  `duration_days` int NOT NULL DEFAULT 30 COMMENT '有效天数',
  `used_by` bigint NULL DEFAULT NULL COMMENT '使用者用户ID',
  `used_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0已使用 1可用 2已禁用)',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '密钥过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_key_code`(`key_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_used_by`(`used_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'VIP激活密钥表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vip_article_heat
-- ----------------------------
DROP TABLE IF EXISTS `vip_article_heat`;
CREATE TABLE `vip_article_heat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `heat_value` int NOT NULL DEFAULT 10 COMMENT '加热值',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文章加热记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vip_member
-- ----------------------------
DROP TABLE IF EXISTS `vip_member`;
CREATE TABLE `vip_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT 'VIP等级(1普通 2高级 3超级)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `expire_time` datetime NOT NULL COMMENT '到期时间',
  `activation_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '激活密钥',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态(0过期 1有效)',
  `heat_count_today` int NULL DEFAULT 0 COMMENT '今日已用加热次数',
  `heat_date` date NULL DEFAULT NULL COMMENT '加热次数重置日期',
  `download_count_today` int NULL DEFAULT 0 COMMENT '今日已用下载次数',
  `download_date` date NULL DEFAULT NULL COMMENT '下载次数重置日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'VIP会员表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
