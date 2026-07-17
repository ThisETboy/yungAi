package com.example.smarthub.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要进行操作日志记录的 Controller 方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 模块标题
     */
    String title() default "";

    /**
     * 业务类型
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作类别
     */
    OperatorType operatorType() default OperatorType.BACK_USER;

    enum BusinessType {
        OTHER,       // 其他
        INSERT,      // 新增
        UPDATE,      // 修改
        DELETE,      // 删除
        IMPORT,      // 导入
        EXPORT,      // 导出
        GRANT,       // 授权
        FORCE_OFFLINE // 强制下线
    }

    enum OperatorType {
        BACK_USER,  // 后台用户
        MOBILE_USER // 手机端用户
    }
}
