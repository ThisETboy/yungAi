package com.example.smarthub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.smarthub.**.mapper")
@EnableAsync
public class SmarthubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmarthubApplication.class, args);
    }
}
