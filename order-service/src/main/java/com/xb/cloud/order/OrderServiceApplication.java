package com.xb.cloud.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单微服务 —— 启动类
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：{@code @EnableFeignClients} 开启 OpenFeign 扫描，
 * 自动发现并代理 {@code @FeignClient} 接口，实现声明式的 HTTP 调用。</p>
 *
 * <p>启动方式：</p>
 * <pre>
 *   mvn -pl order-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
 * </pre>
 *
 * @author ibqy
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}