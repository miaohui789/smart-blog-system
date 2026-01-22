# 系统版本管理功能安装指南

## 快速安装

### 方式1：命令行执行（推荐）

```bash
# Linux/Mac
mysql -u root -p blog_db < docs/database/system_version_complete.sql

# Windows PowerShell
Get-Content "docs/database/system_version_complete.sql" -Encoding UTF8 | mysql -u root -p密码 blog_db
```

### 方式2：MySQL客户端执行

1. 打开MySQL客户端（如Navicat、DBeaver、phpMyAdmin等）
2. 连接到数据库
3. 选择 `blog_db` 数据库
4. 打开并执行 `docs/database/system_version_complete.sql` 文件

### 方式3：命令行交互式执行

```bash
mysql -u root -p
```

然后在MySQL命令行中执行：

```sql
USE blog_db;
SOURCE /path/to/docs/database/system_version_complete.sql;
```

## 安装内容

该脚本会自动完成以下操作：

### 1. 创建数据库表
- ✅ `system_version` - 系统版本表

### 2. 配置管理端菜单
- ✅ 版本管理菜单（ID: 35）
- ✅ 新增版本按钮（ID: 424）
- ✅ 编辑版本按钮（ID: 425）
- ✅ 删除版本按钮（ID: 426）
- ✅ 更新状态按钮（ID: 427）

### 3. 分配角色权限
- ✅ 管理员角色：所有权限
- ✅ 编辑角色：查看、新增、编辑权限

### 4. 插入初始数据
- ✅ 1.0.0 正式版数据

## 安装后操作

### 1. 退出重新登录
1. 点击管理端右上角头像
2. 选择"退出登录"
3. 重新登录管理员账号

### 2. 查看菜单
- 在左侧菜单找到：**系统管理 → 版本管理**

### 3. 测试功能
- 新增版本
- 编辑版本
- 删除版本
- 状态切换
- 访问用户端查看版本历史

## 验证安装

脚本执行后会自动显示验证信息：

1. **菜单配置验证** - 显示5个菜单项
2. **角色权限验证** - 显示管理员和编辑角色的权限数量
3. **版本数据验证** - 显示已插入的版本记录

## 卸载（如需要）

如果需要卸载版本管理功能，执行以下SQL：

```sql
USE blog_db;

-- 删除角色菜单关联
DELETE FROM sys_role_menu WHERE menu_id IN (35, 424, 425, 426, 427);

-- 删除菜单
DELETE FROM sys_menu WHERE id IN (35, 424, 425, 426, 427);

-- 删除版本表
DROP TABLE IF EXISTS system_version;
```

## 常见问题

### Q1: 执行脚本后看不到菜单？
**A:** 需要退出登录重新登录，或清除浏览器缓存后刷新。

### Q2: 提示菜单ID冲突？
**A:** 脚本使用了 `ON DUPLICATE KEY UPDATE`，会自动更新已存在的记录，不会报错。

### Q3: 如何修改菜单ID？
**A:** 如果ID 35或424-427已被占用，可以修改脚本中的ID值，建议使用更大的数字。

### Q4: 如何给其他角色分配权限？
**A:** 在 `sys_role_menu` 表中插入对应的角色ID和菜单ID关联记录。

## 相关文件

- **完整安装脚本**：`docs/database/system_version_complete.sql`
- **功能说明文档**：`docs/系统版本管理功能说明.md`
- **表结构脚本**：`docs/database/system_version.sql`

## 技术支持

如遇到问题，请检查：
1. 数据库连接是否正常
2. 用户是否有足够的权限
3. 数据库字符集是否为 utf8mb4
4. MySQL版本是否支持（建议5.7+）

---

**最后更新**：2026年1月22日
