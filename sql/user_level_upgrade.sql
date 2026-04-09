ALTER TABLE `sys_user`
ADD COLUMN `user_level` tinyint NULL DEFAULT 1 COMMENT '用户等级(1-100)' AFTER `vip_expire_time`;

UPDATE `sys_user`
SET `user_level` = 1
WHERE `user_level` IS NULL OR `user_level` < 1;
