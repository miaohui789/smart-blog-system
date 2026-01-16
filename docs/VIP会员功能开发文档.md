# VIP 会员功能开发文档

## 一、功能概述

为博客系统添加 VIP 会员功能，用户可通过激活密钥晋升为 VIP 身份，享受专属特权。

## 二、功能需求

### 2.1 用户端功能

| 功能 | 描述 |
|------|------|
| 密钥激活 | 用户输入有效密钥激活 VIP 身份 |
| VIP 名片 | 个人中心显示尊贵的 VIP 专属名片 |
| 文章下载 | VIP 用户可下载文章的 Markdown 文档 |
| 文章加热 | VIP 用户可为文章增加热度（每日限制） |
| VIP 标识 | 评论区头像旁显示 VIP 小图标 |
| 会员信息 | 查看 VIP 到期时间、等级等信息 |

### 2.2 管理端功能

| 功能 | 描述 |
|------|------|
| 密钥管理 | 生成、查看、禁用激活密钥 |
| 会员列表 | 查看所有 VIP 会员信息 |
| 会员编辑 | 修改会员等级、到期时间 |
| 会员统计 | VIP 会员数量、激活趋势统计 |

## 三、数据库设计

### 3.1 VIP 会员表 (vip_member)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 ID |
| user_id | bigint | 用户 ID |
| level | tinyint | VIP 等级 (1普通VIP 2高级VIP 3超级VIP) |
| start_time | datetime | 开始时间 |
| expire_time | datetime | 到期时间 |
| activation_key | varchar(64) | 激活密钥 |
| status | tinyint | 状态 (0过期 1有效) |
| heat_count_today | int | 今日已用加热次数 |
| heat_date | date | 加热次数重置日期 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 3.2 VIP 密钥表 (vip_activation_key)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 ID |
| key_code | varchar(64) | 密钥代码 |
| level | tinyint | VIP 等级 |
| duration_days | int | 有效天数 |
| used_by | bigint | 使用者用户 ID |
| used_time | datetime | 使用时间 |
| status | tinyint | 状态 (0已使用 1可用 2已禁用) |
| remark | varchar(255) | 备注 |
| create_time | datetime | 创建时间 |
| expire_time | datetime | 密钥过期时间 |

### 3.3 文章加热记录表 (vip_article_heat)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 ID |
| article_id | bigint | 文章 ID |
| user_id | bigint | 用户 ID |
| heat_value | int | 加热值 |
| create_time | datetime | 创建时间 |

### 3.4 用户表扩展字段

在 `sys_user` 表添加字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| is_vip | tinyint | 是否 VIP (0否 1是) |
| vip_level | tinyint | VIP 等级 |
| vip_expire_time | datetime | VIP 到期时间 |

## 四、VIP 等级权益

| 等级 | 名称 | 下载次数/日 | 加热次数/日 | 加热值 | 专属标识 |
|------|------|------------|------------|--------|---------|
| 1 | 普通 VIP | 5 | 3 | +10 | 铜色 VIP |
| 2 | 高级 VIP | 20 | 10 | +30 | 银色 VIP |
| 3 | 超级 VIP | 无限 | 30 | +100 | 金色 VIP |

## 五、API 接口设计

### 5.1 用户端接口

```
POST   /api/vip/activate          # 激活 VIP（密钥激活）
GET    /api/vip/info              # 获取 VIP 信息
GET    /api/vip/privileges        # 获取 VIP 权益说明
POST   /api/vip/heat/{articleId}  # 文章加热
GET    /api/articles/{id}/download # 下载文章 MD（VIP）
```

### 5.2 管理端接口

```
GET    /api/admin/vip/members           # 会员列表
GET    /api/admin/vip/members/{id}      # 会员详情
PUT    /api/admin/vip/members/{id}      # 编辑会员
DELETE /api/admin/vip/members/{id}      # 删除会员

GET    /api/admin/vip/keys              # 密钥列表
POST   /api/admin/vip/keys/generate     # 批量生成密钥
PUT    /api/admin/vip/keys/{id}/status  # 禁用/启用密钥
DELETE /api/admin/vip/keys/{id}         # 删除密钥

GET    /api/admin/vip/statistics        # VIP 统计数据
```

## 六、前端页面设计

### 6.1 用户端

1. **VIP 激活页面** (`/vip/activate`)
   - 密钥输入框
   - 激活按钮
   - VIP 权益介绍

2. **VIP 中心页面** (`/vip/center`)
   - VIP 名片展示（等级、到期时间）
   - 今日剩余权益（下载次数、加热次数）
   - VIP 特权说明

3. **文章详情页改造**
   - 添加下载按钮（VIP 可见）
   - 添加加热按钮（VIP 可用）

4. **评论区改造**
   - VIP 用户头像旁显示等级图标

### 6.2 管理端

1. **会员管理页面** (`/vip/member`)
   - 会员列表（搜索、筛选、分页）
   - 会员编辑弹窗

2. **密钥管理页面** (`/vip/key`)
   - 密钥列表
   - 批量生成弹窗
   - 密钥状态管理

3. **VIP 统计页面** (`/vip/statistics`)
   - 会员总数、各等级分布
   - 激活趋势图表

## 七、密钥格式

密钥格式：`VIP-XXXX-XXXX-XXXX-XXXX`

- 前缀 `VIP-` 固定
- 后续为 16 位大写字母和数字组合
- 分为 4 组，每组 4 位，用 `-` 分隔

示例：`VIP-A1B2-C3D4-E5F6-G7H8`

## 八、业务流程

### 8.1 密钥激活流程

```
用户输入密钥 → 验证密钥有效性 → 检查是否已是VIP
    ↓                              ↓
密钥无效返回错误              是：延长到期时间
    ↓                              ↓
密钥有效 → 创建/更新VIP记录 → 标记密钥已使用 → 返回成功
```

### 8.2 文章加热流程

```
用户点击加热 → 验证VIP身份 → 检查今日次数
    ↓              ↓            ↓
非VIP提示      VIP过期提示   次数用尽提示
    ↓
次数充足 → 增加文章热度 → 记录加热日志 → 更新今日次数
```

## 九、数据库 SQL 语句

```sql
-- =============================================
-- VIP 会员功能数据库变更
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
```

## 十、开发计划

### 第一阶段：基础功能（3天）
- [ ] 数据库表创建
- [ ] 后端实体类、Mapper、Service
- [ ] 密钥激活接口
- [ ] VIP 信息查询接口

### 第二阶段：核心功能（4天）
- [ ] 文章下载功能
- [ ] 文章加热功能
- [ ] VIP 标识显示
- [ ] 用户端 VIP 页面

### 第三阶段：管理功能（3天）
- [ ] 管理端会员列表
- [ ] 管理端密钥管理
- [ ] 批量生成密钥
- [ ] VIP 统计页面

### 第四阶段：优化完善（2天）
- [ ] VIP 到期自动处理
- [ ] 权限校验完善
- [ ] 界面美化
- [ ] 测试与修复

## 十一、注意事项

1. **安全性**
   - 密钥生成使用安全随机数
   - 激活接口需要登录验证
   - 防止密钥暴力破解（限制尝试次数）

2. **性能**
   - VIP 状态可缓存到 Redis
   - 每日次数重置使用定时任务

3. **用户体验**
   - VIP 即将到期提醒
   - 激活成功动画效果
   - 清晰的权益说明

4. **扩展性**
   - 预留更多 VIP 等级
   - 支持多种激活方式（密钥、支付等）
