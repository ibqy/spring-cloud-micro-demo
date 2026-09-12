package com.xb.cloud.order.client;

import com.xb.cloud.common.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 用户服务 Feign 客户端 —— 声明式远程调用
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
 * <p>注意：{@code url} 属性可用于直连指定地址（如 {@code http://localhost:8081}），
 * 留空则通过服务发现（Nacos / Eureka / 静态列表）解析目标地址。</p>
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserFeignClient extends UserApi {
}