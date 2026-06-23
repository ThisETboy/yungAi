-- ============================================================
-- smarthub 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS `smarthub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smarthub`;

-- ----------------------------
-- 系统管理模块
-- ----------------------------

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`           BIGINT       NOT NULL COMMENT '用户ID',
    `username`     VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`     VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`     VARCHAR(64)  DEFAULT '' COMMENT '昵称',
    `email`        VARCHAR(128) DEFAULT '' COMMENT '邮箱',
    `phone`        VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    `avatar`       VARCHAR(256) DEFAULT '' COMMENT '头像URL',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0=未删除,1=已删除)',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL COMMENT '角色ID',
    `role_code`   VARCHAR(64)  NOT NULL COMMENT '角色编码',
    `role_name`   VARCHAR(128) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(256) DEFAULT '' COMMENT '描述',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB COMMENT='角色表';

-- 菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`          BIGINT        NOT NULL COMMENT '菜单ID',
    `parent_id`   BIGINT        NOT NULL DEFAULT 0 COMMENT '父菜单ID(0=顶级)',
    `menu_type`   TINYINT       NOT NULL COMMENT '类型(1=目录,2=菜单,3=按钮)',
    `menu_name`   VARCHAR(128)  NOT NULL COMMENT '菜单名称',
    `route_path`  VARCHAR(256)  DEFAULT '' COMMENT '路由路径(前端vue-router)',
    `component`   VARCHAR(256)  DEFAULT '' COMMENT '组件路径',
    `icon`        VARCHAR(128)  DEFAULT '' COMMENT '图标',
    `sort_order`  INT           NOT NULL DEFAULT 0 COMMENT '排序',
    `perms`       VARCHAR(256)  DEFAULT '' COMMENT '权限标识',
    `visible`     TINYINT       NOT NULL DEFAULT 1 COMMENT '是否显示(0=隐藏,1=显示)',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='菜单表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`      BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `id`      BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
    CONSTRAINT `fk_rp_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ----------------------------
-- AI 模块
-- ----------------------------

-- AI 模型配置表
DROP TABLE IF EXISTS `ai_model_config`;
CREATE TABLE `ai_model_config` (
    `id`              BIGINT       NOT NULL COMMENT '配置ID',
    `model_name`      VARCHAR(128) NOT NULL COMMENT '模型显示名称',
    `provider`        VARCHAR(32)  NOT NULL COMMENT '提供商(ollama/dashscope/anthropic/deepseek)',
    `endpoint_model`  VARCHAR(128) NOT NULL COMMENT '端点模型名(API调用时使用的模型名)',
    `enabled`         TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用(0=禁用,1=启用)',
    `sort_order`      INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='AI模型配置表';

-- AI 会话表
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
    `id`          BIGINT      NOT NULL COMMENT '会话ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(256) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    `model_id`    BIGINT      DEFAULT NULL COMMENT '模型ID',
    `context`     TEXT        DEFAULT NULL COMMENT '上下文(JSON格式)',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_conv_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB COMMENT='AI会话表';

-- AI 消息表
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message` (
    `id`             BIGINT       NOT NULL COMMENT '消息ID',
    `conversation_id` BIGINT      NOT NULL COMMENT '会话ID',
    `role`           VARCHAR(16)  NOT NULL COMMENT '角色(user/assistant/system)',
    `content`        LONGTEXT     NOT NULL COMMENT '消息内容',
    `tokens_in`      INT          DEFAULT 0 COMMENT '输入token数',
    `tokens_out`     INT          DEFAULT 0 COMMENT '输出token数',
    `model_used`     VARCHAR(64)  DEFAULT NULL COMMENT '使用的模型名快照',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`),
    CONSTRAINT `fk_msg_conv` FOREIGN KEY (`conversation_id`) REFERENCES `ai_conversation` (`id`)
) ENGINE=InnoDB COMMENT='AI消息表';
