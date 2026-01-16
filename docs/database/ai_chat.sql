-- AI聊天功能数据库表
-- 创建时间: 2026-01-13

-- AI配置表（管理端配置）
CREATE TABLE IF NOT EXISTS `ai_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `provider` VARCHAR(50) NOT NULL DEFAULT 'deepseek' COMMENT 'AI服务商: deepseek, openai, zhipu, qianfan, dashscope',
    `model` VARCHAR(100) NOT NULL DEFAULT 'deepseek-chat' COMMENT '模型名称',
    `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥(加密存储)',
    `base_url` VARCHAR(255) DEFAULT NULL COMMENT 'API基础地址',
    `max_tokens` INT DEFAULT 2000 COMMENT '最大token数',
    `temperature` DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数(0-1)',
    `system_prompt` TEXT COMMENT '系统提示词',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0-禁用, 1-启用',
    `daily_limit` INT DEFAULT 100 COMMENT '每用户每日请求限制',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI配置表';

-- AI对话会话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) DEFAULT '新对话' COMMENT '会话标题',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user, assistant',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `tokens` INT DEFAULT 0 COMMENT '消耗token数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- AI使用统计表（用于限流）
CREATE TABLE IF NOT EXISTS `ai_usage` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `date` DATE NOT NULL COMMENT '日期',
    `request_count` INT DEFAULT 0 COMMENT '请求次数',
    `total_tokens` INT DEFAULT 0 COMMENT '消耗token总数',
    UNIQUE KEY `uk_user_date` (`user_id`, `date`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI使用统计表';

-- 插入默认配置（DeepSeek）
INSERT INTO `ai_config` (`provider`, `model`, `api_key`, `base_url`, `max_tokens`, `temperature`, `system_prompt`, `enabled`, `daily_limit`)
VALUES (
    'deepseek',
    'deepseek-chat',
    '',  -- 需要在管理端配置真实的API Key
    'https://api.deepseek.com',
    2000,
    0.70,
    '你是小博，一个专业的计算机技术博客AI助手。你擅长解答编程、软件开发、计算机科学等技术问题。请用简洁、准确、专业的语言回答用户的问题，必要时可以提供代码示例。',
    0,  -- 默认禁用，配置API Key后启用
    100
);
