-- 消息撤回功能 - 添加 is_withdrawn 字段
ALTER TABLE `message` ADD COLUMN `is_withdrawn` TINYINT(1) DEFAULT 0 COMMENT '是否已撤回：0否 1是' AFTER `is_read`;
