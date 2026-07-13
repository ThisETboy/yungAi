-- ============================================================
-- SmartHub 菜单补全 — 添加缺失的菜单记录
-- ============================================================
-- 用途：为前端已实现但数据库缺少菜单记录的页面补充菜单
-- 执行方式：mysql -u root -p smarthub < V10__fix_missing_menus.sql
-- ============================================================
USE `smarthub`;

-- 新增一级菜单：数据中心
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(60, 0, 1, '数据中心', '/datacenter', NULL, 'data-line', 6, '', 1);

-- 数据中心子菜单：数据字典
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(61, 60, 2, '数据字典', '/dict', 'system/dict/DictManage', 'collection', 1, 'sys:dict:type:list', 1);

-- 数据字典按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(610, 61, 3, '新增字典类型', '', '', '', 1, 'sys:dict:type:add', 1),
(611, 61, 3, '编辑字典类型', '', '', '', 2, 'sys:dict:type:edit', 1),
(612, 61, 3, '删除字典类型', '', '', '', 3, 'sys:dict:type:delete', 1),
(613, 61, 3, '新增字典数据', '', '', '', 4, 'sys:dict:data:add', 1),
(614, 61, 3, '编辑字典数据', '', '', '', 5, 'sys:dict:data:edit', 1),
(615, 61, 3, '删除字典数据', '', '', '', 6, 'sys:dict:data:delete', 1);

-- 数据中心子菜单：操作日志
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(62, 60, 2, '操作日志', '/oper-log', 'system/operlog/OperLogManage', 'document', 2, 'sys:operlog:list', 1);

-- 操作日志按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(620, 62, 3, '删除操作日志', '', '', '', 1, 'sys:operlog:delete', 1);

-- 数据中心子菜单：文件管理
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(63, 60, 2, '文件管理', '/files', 'system/file/FileManage', 'folder-opened', 3, 'sys:file:upload', 1);

-- 文件管理按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(630, 63, 3, '上传文件', '', '', '', 1, 'sys:file:upload', 1),
(631, 63, 3, '下载文件', '', '', '', 2, 'sys:file:download', 1);

-- 数据中心子菜单：系统配置
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(64, 60, 2, '系统配置', '/config', 'system/config/SysConfigManage', 'setting', 4, 'sys:config:list', 1);

-- 系统配置按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(640, 64, 3, '新增配置', '', '', '', 1, 'sys:config:add', 1),
(641, 64, 3, '编辑配置', '', '', '', 2, 'sys:config:edit', 1),
(642, 64, 3, '删除配置', '', '', '', 3, 'sys:config:delete', 1);

-- 数据中心子菜单：缓存管理
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(65, 60, 2, '缓存管理', '/cache', 'system/cache/CacheManage', 'refresh', 5, 'sys:cache:list', 1);

-- 缓存管理按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(650, 65, 3, '查看缓存', '', '', '', 1, 'sys:cache:view', 1),
(651, 65, 3, '删除缓存', '', '', '', 2, 'sys:cache:delete', 1);

-- 数据中心子菜单：登录日志
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(66, 60, 2, '登录日志', '/login-log', 'system/loginlog/LoginLogManage', 'time', 6, 'sys:loginlog:list', 1);

-- 个人中心
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(70, 0, 1, '个人中心', '/profile', NULL, 'user', 7, '', 1),
(71, 70, 2, '个人资料', '/profile', 'profile/ProfileView', 'user-filled', 1, 'sys:profile:edit', 1);

-- 个人中心的按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_type`, `menu_name`, `route_path`, `component`, `icon`, `sort_order`, `perms`, `visible`) VALUES
(710, 71, 3, '修改资料', '', '', '', 1, 'sys:profile:edit', 1),
(711, 71, 3, '修改密码', '', '', '', 2, 'sys:profile:password', 1);

-- 角色-菜单关联：管理员拥有所有新增菜单
INSERT INTO `sys_role_permission` (`id`, `role_id`, `menu_id`)
SELECT 2000 + m.id, 1, m.id FROM `sys_menu` m
WHERE m.id IN (60, 61, 610, 611, 612, 613, 614, 615,
               62, 620,
               63, 630, 631,
               64, 640, 641, 642,
               65, 650, 651,
               66,
               70, 71, 710, 711)
  AND m.deleted = 0;
