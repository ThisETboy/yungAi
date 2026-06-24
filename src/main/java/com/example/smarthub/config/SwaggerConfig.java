package com.example.smarthub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / Knife4j API 文档配置
 * 将接口分为 6 个组：认证、用户、角色、菜单、AI、协议
 * 支持 Bearer Token JWT 认证
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("smarthub API 文档")
                        .description("企业级 AI 智能管理平台接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("smarthub-team")))
                .addSecurityItem(new SecurityRequirement().addList("BearerToken"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerToken",
                                new SecurityScheme()
                                        .name("BearerToken")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-认证管理")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2-用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roleApi() {
        return GroupedOpenApi.builder()
                .group("3-角色管理")
                .pathsToMatch("/api/roles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi menuApi() {
        return GroupedOpenApi.builder()
                .group("4-菜单管理")
                .pathsToMatch("/api/menus/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("5-AI功能")
                .pathsToMatch("/api/ai/**")
                .build();
    }

    @Bean
    public GroupedOpenApi protocolApi() {
        return GroupedOpenApi.builder()
                .group("6-协议管理")
                .pathsToMatch("/api/protocol/**")
                .build();
    }
}
