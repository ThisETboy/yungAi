package com.example.smarthub.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 在 INSERT / UPDATE 时自动填充 createTime, updateTime, createBy, updateBy 字段
 * 注意：当前 createBy/updateBy 硬编码为 "system"，后续可结合 SecurityContext 改为当前登录用户
 */
@Slf4j
@Component
public class MyBatisMetaHandler implements MetaObjectHandler {

    /** 插入时自动填充 */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createBy", () -> "system", String.class);
        this.strictInsertFill(metaObject, "updateBy", () -> "system", String.class);
    }

    /** 更新时自动填充 */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateBy", () -> "system", String.class);
    }
}
