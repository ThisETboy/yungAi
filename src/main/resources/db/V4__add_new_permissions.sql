-- ============================================================
-- SmartHub 权限扩展 — 新增协议管理、AI 模型配置、请求日志等权限
-- ============================================================
-- 用途：为新增的功能模块添加菜单记录和权限标识
-- 执行方式：mysql -u root -p smarthub < V4__add_new_permissions.sql
-- ============================================================
USE `smarthub`;

-- 新增菜单：协议管理
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(30, 0, 1, '协议管理', '/protocol', NULL, 'link', 4, '', 1),
(31, 30, 2, '协议状态', '/protocol', 'protocol/ProtocolManage', 'connection', 1, 'sys:protocol:list', 1);

-- 协议管理按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(310, 31, 3, '发送数据', '', '', '', 1, 'sys:protocol:send', 1),
(311, 31, 3, '启动协议', '', '', '', 2, 'sys:protocol:start', 1),
(312, 31, 3, '停止协议', '', '', '', 3, 'sys:protocol:stop', 1);

-- 新增菜单：AI 模型配置
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(40, 2, 2, 'AI 模型配置', '/ai-models', 'ai/modelconfig/AiModelConfigManage', 'monitor', 2, 'sys:ai:model', 1);

-- 新增菜单：请求日志
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(50, 0, 1, '监控中心', '/monitor', NULL, 'chart', 5, '', 1),
(51, 50, 2, '请求日志', '/logs', 'monitor/RequestLogManage', 'document', 1, 'sys:log:list', 1);

-- 角色-菜单关联：管理员拥有所有新增菜单
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`)
SELECT 1000 + m.id, 1, m.id FROM `sys_menu` m
WHERE m.id IN (30, 31, 310, 311, 312, 40, 50, 51) AND m.deleted = 0;

-- 普通用户拥有 AI 聊天 + 代码生成 + AI 模型配置
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`) VALUES
(202, 2, 40);
