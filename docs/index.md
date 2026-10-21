---
layout: home

hero:
  name: Spring Cloud 微服务教学
  text: 最小可跑的用户-订单微服务
  tagline: Gateway 网关 → order 服务 → user 服务的完整调用链路，演示服务拆分、OpenFeign 声明式调用与网关路由
  actions:
    - theme: brand
      text: 开始学习 →
      link: /01-architecture
    - theme: alt
      text: GitHub 源码
      link: https://github.com/ibqy/spring-cloud-micro-demo

features:
  - icon: 🚪
    title: Gateway 网关（8080）
    details: 统一入口、路由转发，体会网关在微服务架构中的位置
  - icon: 📦
    title: order 服务（8082）
    details: 订单业务，通过 OpenFeign 跨服务调用 user 服务
  - icon: 👤
    title: user 服务（8081）
    details: 用户数据提供方，被下游声名式调用
  - icon: 🔗
    title: OpenFeign 服务调用
    details: 声明式 HTTP 客户端，像调本地方法一样调远程服务
  - icon: 📝
    title: 教学知识点清单
    details: 诚实地标注本仓库覆盖与未覆盖的内容，以及进阶方向
---

## 课程结构

| 章节 | 主题 | 关键代码 |
|:---|:---|:---|
| [01 · 整体架构](/01-architecture) | 服务拆分、调用链、模块职责 | — |
| [02 · 教学知识点](/02-教学知识点) | 覆盖内容与未覆盖边界 | — |
| [03 · 网关路由与限流](/03-gateway-routing-ratelimit) | Route / Predicate / Filter、令牌桶限流 | `application.yml` / `GatewayConfig.ipKeyResolver` |
| [04 · OpenFeign 与熔断降级](/04-feign-circuitbreaker) | 契约模块、Feign、Resilience4j、fallback | `UserFeignClient` / `UserFallbackHandler` |
| [05 · 启动与链路验证](/05-run-and-verify) | 启动顺序、分段验证、主动触发熔断 | — |

## 代码地图

| 模块 | 关键类 | 职责 |
|:---|:---|:---|
| `common` | `UserApi` / `OrderApi` / `Result` | 服务间契约与统一响应 |
| `user-service` | `UserController` / `UserService` | 用户 CRUD（内存存储） |
| `order-service` | `OrderController` / `UserFeignClient` / `UserFallbackHandler` / `CircuitBreakerEventLogger` | 订单业务 + 跨服务调用 + 降级 + 熔断状态日志 |
| `gateway-service` | `GatewayConfig` | 限流 KeyResolver（路由在 `application.yml`） |

## 在路线中的位置

> **Java × AI 进阶路线** · 阶段 1「架构与资金安全」第 2 站  
> 上一站：[DDD 领域驱动设计](https://ibqy.github.io/ddd-arch-demo/) ｜ 下一站：[Spring Boot 高阶特性](https://ibqy.github.io/spring-advanced-demo/)  
> 完整路线：[ibqy.github.io/curriculum](https://ibqy.github.io/curriculum)

## 前置条件

- JDK 21 + Maven 3.6.3+（实测 3.6.3 可构建）
- 验证限流需要本地 Redis（默认 `localhost:6379`）
