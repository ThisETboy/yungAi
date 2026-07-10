-- ============================================================
-- SmartHub 登录日志表
-- ============================================================
USE `smarthub`;

DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `username`        VARCHAR(64)  NOT NULL COMMENT '用户名',
    `ip_address`      VARCHAR(128) DEFAULT '' COMMENT '登录IP',
    `user_agent`      VARCHAR(512) DEFAULT '' COMMENT '浏览器UA',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=失败,1=成功)',
    `message`         VARCHAR(256) DEFAULT '' COMMENT '提示信息',
    `login_time`      DATETIME     NOT NULL COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB COMMENT='登录日志表';
