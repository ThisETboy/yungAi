-- ============================================================
-- SmartHub 操作日志表
-- ============================================================
USE `smarthub`;

DROP TABLE IF EXISTS `oper_log`;
CREATE TABLE `oper_log` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `title`           VARCHAR(128) DEFAULT '' COMMENT '模块标题',
    `business_type`   INT          NOT NULL DEFAULT 0 COMMENT '业务类型（0=其他,1=新增,2=修改,3=删除,4=导入,5=导出,6=授权）',
    `method_name`     VARCHAR(128) DEFAULT '' COMMENT '方法名称',
    `operator_type`   INT          NOT NULL DEFAULT 0 COMMENT '操作类别（1=后台用户,2=手机端用户）',
    `oper_name`       VARCHAR(64)  DEFAULT '' COMMENT '操作人员',
    `oper_url`        VARCHAR(256) DEFAULT '' COMMENT '请求URL',
    `oper_ip`         VARCHAR(128) DEFAULT '' COMMENT '主机地址',
    `oper_param`      VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
    `json_result`     VARCHAR(2000) DEFAULT '' COMMENT '返回参数',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '操作状态（0=异常,1=正常）',
    `error_msg`       VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    `oper_time`       DATETIME     NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_time` (`oper_time`),
    KEY `idx_oper_name` (`oper_name`),
    KEY `idx_title` (`title`)
) ENGINE=InnoDB COMMENT='操作日志表';
