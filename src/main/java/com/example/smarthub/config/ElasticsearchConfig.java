package com.example.smarthub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Elasticsearch 配置
 * 创建一个指向 ES 集群的 WebClient，设置 10 秒超时
 * 当前仅作为基础设施 Bean 注册，尚未在 Controller/Service 中使用
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Bean
    public WebClient elasticsearchWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10));
        return WebClient.builder()
                .baseUrl(uris)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
