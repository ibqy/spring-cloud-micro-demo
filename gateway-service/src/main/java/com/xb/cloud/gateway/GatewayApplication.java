package com.xb.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway —— 启动类
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：Gateway 基于 Spring WebFlux（Reactive），
 * 不引入 spring-boot-starter-web，避免与 Netty 冲突。
 * 它是所有微服务的统一入口，负责路由转发、过滤、限流、跨域等。</p>
 *
 * <p>启动方式：</p>
 * <pre>
 *   mvn -pl gateway-service spring-boot:run
 * </pre>
 *
 * @author ibqy
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}