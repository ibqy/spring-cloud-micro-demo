# Spring Cloud 微服务架构说明

## 背景

微服务架构将单体应用拆分为多个独立部署的服务，每个服务只关注一个业务领域。本项目通过"用户-订单"这个经典电商场景，演示 Spring Cloud 的核心组件如何协同工作。

## 架构总览

```
外部请求 → Gateway :8080
                ├── /api/users/** → user-service :8081  (用户 CRUD)
                └── /api/orders/** → order-service :8082 (订单 CRUD)
                                        │
                                   OpenFeign 调用
                                        ↓
                                  user-service :8081
```

## 核心流程

1. 用户发请求到 `localhost:8080/api/orders/1`
2. Gateway 根据路径匹配到 order-service 路由
3. Gateway 自动注入 `X-Request-Source: gateway` 请求头
4. order-service 收到请求，调用 OrderController
5. OrderController 通过 UserFeignClient（OpenFeign）调用 user-service 获取用户信息
6. 响应原路返回：order-service → Gateway → 用户

## 各模块职责

| 模块 | 职责 | 关键依赖 |
|------|------|----------|
| common | 共享 DTO + API 接口契约 | 无 |
| user-service | 用户增删改查 | Spring Web |
| order-service | 订单增删改查 + OpenFeign 调用用户服务 | Spring Web + OpenFeign |
| gateway-service | 路由转发 + 过滤器 + 限流 | Spring Cloud Gateway |