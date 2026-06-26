-- ============================================================
-- 请求日志表 — 用于 AOP 切面记录关键请求信息
-- ============================================================

USE `smarthub`;

DROP TABLE IF EXISTS `request_log`;
CREATE TABLE `request_log` (
    `id`              BIGINT        NOT NULL COMMENT '主键ID',
    `method`          VARCHAR(10)   NOT NULL DEFAULT '' COMMENT '请求方法(GET/POST/PUT/DELETE)',
    `url`             VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '请求URL',
    `params`          TEXT          DEFAULT NULL COMMENT '请求参数(JSON, 截断至2000字符)',
    `status_code`     INT           NOT NULL DEFAULT 200 COMMENT '响应状态码',
    `response_body`   TEXT          DEFAULT NULL COMMENT '响应结果摘要(截断至2000字符)',
    `duration_ms`     BIGINT        NOT NULL DEFAULT 0 COMMENT '请求耗时(毫秒)',
    `ip_address`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '请求IP地址',
    `operator`        VARCHAR(64)   DEFAULT '' COMMENT '操作用户名',
    `user_agent`      VARCHAR(512)  DEFAULT '' COMMENT '浏览器UA',
    `module`          VARCHAR(32)   DEFAULT '' COMMENT '请求模块(system/ai/protocol)',
    `is_error`        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否异常(0=正常,1=异常)',
    `error_msg`       VARCHAR(1000) DEFAULT '' COMMENT '异常信息(截断至1000字符)',
    `create_by`       VARCHAR(64)   DEFAULT '' COMMENT '创建人',
    `update_by`       VARCHAR(64)   DEFAULT '' COMMENT '更新人',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_module` (`module`),
    KEY `idx_operator` (`operator`),
    KEY `idx_is_error` (`is_error`)
) ENGINE=InnoDB COMMENT='请求日志表';
