package com.xb.cloud.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Gateway 全局配置 —— 路由、过滤器、限流
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：
 * <ul>
 *   <li>路由（Route）：用 {@code RouteLocator} 定义转发规则</li>
 *   <li>过滤器（Filter）：{@code AddRequestHeader} / {@code StripPrefix} 等</li>
 *   <li>限流（RateLimiter）：{@code KeyResolver} 按 IP 限流</li>
 * </ul>
 *
 * <p>注意：路由也可以通过 {@code application.yml} 配置，这里是 Java DSL 方式。</p>
 */
@Configuration
public class GatewayConfig {

    /** 按 IP 限流的 Key 解析器 */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}