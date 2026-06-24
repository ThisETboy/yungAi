package com.example.smarthub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * SmartHub 应用入口
 *
 * 启用特性：
 * - @MapperScan: 自动扫描 module 下所有 mapper 包中的 @Mapper 接口
 * - @EnableAsync: 启用异步方法支持（如 ProtocolServiceImpl.startAllProtocols、RequestLogService.saveLogAsync）
 * - @EnableAspectJAutoProxy: 启用 AOP 自动代理（如 RequestLogAspect 请求日志切面）
 */
@SpringBootApplication
@MapperScan("com.example.smarthub.**.mapper")
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SmarthubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmarthubApplication.class, args);
    }
}
