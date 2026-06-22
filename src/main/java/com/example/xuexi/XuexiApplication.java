package com.example.xuexi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.xuexi.**.mapper")
@EnableAsync
public class XuexiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuexiApplication.class, args);
    }
}
