-- ============================================================
-- xuexi 种子数据
-- ============================================================
USE `xuexi`;

-- 管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'admin@example.com', 1);

-- 普通用户 (密码: user123)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`) VALUES
(2, 'user', '$2a$10$YhZHq.5TFqGXH2SZDi5sMO6rrO0S6KGfONVvJ1b3gcCvJqE5qGKzq', '普通用户', 'user@example.com', 1);

-- 角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'ADMIN', '管理员', '系统管理员，拥有所有权限'),
(2, 'USER', '普通用户', '普通用户，仅可使用基础功能');

-- 用户角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1),
(2, 2, 2);

-- 菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
-- 一级菜单
(1, 0, 1, '系统管理', '/system', NULL, 'setting', 1, '', 1),
(2, 0, 1, 'AI 助手', '/ai', NULL, 'magic', 2, '', 1),
(3, 0, 1, 'AI 代码生成', '/codegen', NULL, 'code', 3, '', 1),
-- 系统管理子菜单
(10, 1, 2, '用户管理', '/system/user', 'system/user/UserManage', 'user', 1, 'sys:user:list', 1),
(11, 1, 2, '角色管理', '/system/role', 'system/role/RoleManage', 'role', 2, 'sys:role:list', 1),
(12, 1, 2, '菜单管理', '/system/menu', 'system/menu/MenuManage', 'menu', 3, 'sys:menu:list', 1),
-- 用户管理按钮权限
(100, 10, 3, '新增用户', '', '', '', 1, 'sys:user:add', 1),
(101, 10, 3, '编辑用户', '', '', '', 2, 'sys:user:edit', 1),
(102, 10, 3, '删除用户', '', '', '', 3, 'sys:user:delete', 1),
-- 角色管理按钮权限
(110, 11, 3, '新增角色', '', '', '', 1, 'sys:role:add', 1),
(111, 11, 3, '编辑角色', '', '', '', 2, 'sys:role:edit', 1),
(112, 11, 3, '删除角色', '', '', '', 3, 'sys:role:delete', 1),
(113, 11, 3, '分配权限', '', '', '', 4, 'sys:role:assign', 1),
-- 菜单管理按钮权限
(120, 12, 3, '新增菜单', '', '', '', 1, 'sys:menu:add', 1),
(121, 12, 3, '编辑菜单', '', '', '', 2, 'sys:menu:edit', 1),
(122, 12, 3, '删除菜单', '', '', '', 3, 'sys:menu:delete', 1),
-- AI 助手子菜单
(20, 2, 2, 'AI 聊天', '/ai/chat', 'ai/chat/ChatView', 'chat', 1, 'ai:chat', 1),
(21, 3, 2, '代码生成', '/ai/codegen', 'ai/codegen/CodeGenView', 'code', 1, 'ai:codegen', 1);

-- 角色菜单关联 (ADMIN 拥有所有菜单)
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`)
SELECT 0, 1, m.id FROM `sys_menu` m WHERE m.deleted = 0;

-- 普通用户只拥有基础菜单
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`) VALUES
(100, 2, 2),   -- AI 助手
(101, 2, 20),  -- AI 聊天
(102, 2, 3);   -- AI 代码生成

-- AI 模型配置
INSERT INTO `ai_model_config` (`id`, `model_name`, `provider`, `endpoint_model`, `enabled`, `sort_order`) VALUES
(1, 'Qwen 2.5 7B (本地)', 'ollama', 'qwen2.5:7b', 1, 1),
(2, '通义千问 Plus', 'dashscope', 'qwen-plus', 1, 2),
(3, 'Claude Sonnet', 'anthropic', 'claude-sonnet-4-20250514', 1, 3),
(4, 'DeepSeek Chat', 'deepseek', 'deepseek-chat', 1, 4);
