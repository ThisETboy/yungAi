-- ============================================================
-- SmartHub 文件信息表
-- ============================================================
USE `smarthub`;

DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
    `id`              BIGINT       NOT NULL COMMENT '主键ID',
    `file_name`       VARCHAR(256) NOT NULL COMMENT '原始文件名',
    `file_url`        VARCHAR(512) NOT NULL COMMENT '文件访问URL',
    `file_size`       BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type`       VARCHAR(64)  DEFAULT '' COMMENT '文件类型(MIME)',
    `file_ext`        VARCHAR(16)  DEFAULT '' COMMENT '文件扩展名',
    `storage_path`    VARCHAR(512) DEFAULT '' COMMENT '存储物理路径',
    `upload_by`       VARCHAR(64)  DEFAULT '' COMMENT '上传人',
    `remark`          VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_upload_by` (`upload_by`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB COMMENT='文件信息表';
