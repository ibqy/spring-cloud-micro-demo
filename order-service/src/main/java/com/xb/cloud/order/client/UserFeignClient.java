package com.xb.cloud.order.client;

import com.xb.cloud.common.api.UserApi;
import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import com.xb.cloud.order.fallback.UserFallbackHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 用户服务 Feign 客户端 —— 声明式远程调用 + 熔断降级
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：{@code @FeignClient(name = "user-service")} 告诉 OpenFeign：
 * <ol>
 *   <li>这是一个远程服务调用接口，目标服务名称为 {@code user-service}</li>
 *   <li>继承 {@link UserApi} 接口，自动获得 {@code /api/users/*} 的调用能力</li>
 *   <li>配合负载均衡（LoadBalancer），即使 user-service 多实例部署也能自动选择可用实例</li>
 * </ol>
 *
 * <p><b>高阶实战</b>：{@code @CircuitBreaker} 为远程调用添加熔断保护。
 * 当 user-service 连续失败达到阈值时，熔断器打开，后续请求直接走 fallback 降级，
 * 避免雪崩效应。配置见 {@code application.yml} 中的 {@code resilience4j.circuitbreaker}。</p>
 *
 * <p>注意：{@code url} 属性可用于直连指定地址（如 {@code http://localhost:8081}），
 * 留空则通过服务发现（Nacos / Eureka / 静态列表）解析目标地址。</p>
 *
 * @author ibqy
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserFeignClient extends UserApi {

    /**
     * 重写父接口方法，添加熔断降级。
     *
     * <p>{@code name = "userService"} 对应 application.yml 中的配置名称。
     * {@code fallbackMethod} 指定降级处理方法，签名必须与原方法一致。</p>
     */
    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    Result<UserDTO> getUserById(Long id);
}