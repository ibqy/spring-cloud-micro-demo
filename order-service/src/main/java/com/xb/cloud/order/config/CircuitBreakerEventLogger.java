package com.xb.cloud.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 熔断器状态迁移日志 —— 让 CLOSED / OPEN / HALF_OPEN 变化可见
 *
 * <p>作者：ibqy | 日期：2026-10-21</p>
 *
 * <p><b>教学知识点</b>：Resilience4j 2.x 默认只发布状态迁移事件，不会主动打印日志。
 * 想看到 {@code CLOSED -> OPEN -> HALF_OPEN -> CLOSED}，需要自己订阅
 * {@link CircuitBreaker#getEventPublisher()}。</p>
 *
 * <p>{@code userService} 熔断器是首次调用时才惰性创建的，因此除了遍历已存在的实例，
 * 还要通过注册中心的 {@code onEntryAdded} 订阅后续创建的实例。</p>
 *
 * @author ibqy
 */
@Component
public class CircuitBreakerEventLogger {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerEventLogger.class);

    public CircuitBreakerEventLogger(CircuitBreakerRegistry registry) {
        registry.getAllCircuitBreakers().forEach(this::logStateTransition);
        registry.getEventPublisher().onEntryAdded(event -> logStateTransition(event.getAddedEntry()));
    }

    private void logStateTransition(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher().onStateTransition(event ->
                log.info("CircuitBreaker '{}' changed state from {} to {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }
}
