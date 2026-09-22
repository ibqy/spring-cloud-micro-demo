package com.xb.cloud.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户微服务 —— 启动类
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：用户服务是一个独立的 Spring Boot 应用，提供用户 CRUD 接口。
 * 通过 {@code @SpringBootApplication} 启动嵌入式 Tomcat，监听 {@code server.port} 端口。
 *
 * <p>启动方式：</p>
 * <pre>
 *   mvn -pl user-service spring-boot:run
 *   # 或指定端口：mvn -pl user-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
 * </pre>
 *
 * @author ibqy
 */
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}