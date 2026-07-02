-- ============================================================
-- SmartHub 系统配置表
-- ============================================================
USE `smarthub`;

DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `config_name`     VARCHAR(128) NOT NULL COMMENT '配置名称',
    `config_key`      VARCHAR(128) NOT NULL COMMENT '配置键（唯一）',
    `config_value`    VARCHAR(2000) DEFAULT '' COMMENT '配置值',
    `config_type`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否系统内置(0=否,1=是)',
    `remark`          VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB COMMENT='系统配置表';

-- 插入默认配置
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_type`, `remark`) VALUES
(1, '系统名称', 'sys.name', 'SmartHub 智能管理平台', 1, '系统显示名称'),
(2, '用户密码初始值', 'sys.user.initPassword', '123456', 1, '新用户初始密码'),
(3, '密码修改周期(天)', 'sys.user.passwordCycle', '90', 1, '用户密码强制修改周期'),
(4, '登录失败锁定次数', 'sys.login.maxRetryCount', '5', 1, '连续登录失败锁定次数'),
(5, '登录失败锁定时间(分钟)', 'sys.login.lockTime', '30', 1, '登录失败锁定时间'),
(6, '文件最大上传大小(MB)', 'sys.file.maxSize', '10', 1, '文件上传大小限制'),
(7, '首页统计刷新间隔(秒)', 'sys.dashboard.refreshInterval', '30', 1, '首页数据统计刷新间隔');
