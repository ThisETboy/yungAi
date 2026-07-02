-- ============================================================
-- SmartHub 数据字典表
-- ============================================================
USE `smarthub`;

-- 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `dict_name`       VARCHAR(128) NOT NULL COMMENT '字典名称',
    `dict_type`       VARCHAR(128) NOT NULL COMMENT '字典类型（英文标识，如 sys_user_status）',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `remark`          VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB COMMENT='字典类型表';

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `dict_type_id`    BIGINT       NOT NULL COMMENT '所属字典类型ID',
    `dict_label`      VARCHAR(128) NOT NULL COMMENT '字典标签',
    `dict_value`      VARCHAR(128) NOT NULL COMMENT '字典值',
    `dict_sort`       INT          NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    `css_class`       VARCHAR(128) DEFAULT NULL COMMENT 'CSS 样式（如 el-tag type）',
    `list_class`      VARCHAR(128) DEFAULT NULL COMMENT '表格回显样式（success/danger/warning/info）',
    `is_default`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认(0=否,1=是)',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_id` (`dict_type_id`)
) ENGINE=InnoDB COMMENT='字典数据表';

-- 插入常用字典类型和数据
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`) VALUES
(1, '用户状态', 'sys_user_status', 1, '用户启用/禁用状态'),
(2, '菜单类型', 'sys_menu_type', 1, '目录/菜单/按钮'),
(3, '性别', 'sys_gender', 1, '男/女/未知'),
(4, '通知类型', 'sys_notice_type', 1, '系统通知/公告/消息'),
(5, '操作类型', 'sys_oper_type', 1, '操作日志类型'),
(6, '是否', 'sys_yes_no', 1, '是/否');

-- 用户状态
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `is_default`) VALUES
(101, 1, '启用',   '1', 1, 'success', 1),
(102, 1, '禁用',   '0', 2, 'danger',  0);

-- 菜单类型
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `is_default`) VALUES
(201, 2, '目录',   '1', 1, 'warning', 1),
(202, 2, '菜单',   '2', 2, 'success', 0),
(203, 2, '按钮',   '3', 3, 'info',    0);

-- 性别
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `is_default`) VALUES
(301, 3, '男', '0', 1, '', 1),
(302, 3, '女', '1', 2, '', 0),
(303, 3, '未知', '2', 3, '', 0);

-- 操作类型
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `is_default`) VALUES
(401, 5, '其他',    '0', 1, '', 1),
(402, 5, '新增',    '1', 2, 'success', 0),
(403, 5, '修改',    '2', 3, 'warning', 0),
(404, 5, '删除',    '3', 4, 'danger',  0),
(405, 5, '导入',    '4', 5, '', 0),
(406, 5, '导出',    '5', 6, '', 0),
(407, 5, '授权',    '6', 7, 'info',    0),
(408, 5, '卸载',    '7', 8, '', 0);

-- 是否
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `dict_sort`, `list_class`, `is_default`) VALUES
(501, 6, '是', '1', 1, 'success', 1),
(502, 6, '否', '0', 2, 'danger',  0);
