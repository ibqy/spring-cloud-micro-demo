# 🌩 Spring Cloud 微服务教学项目

> 作者：xb ｜ 日期：2026-09-12
>
> 一个从零搭建的 Spring Cloud 微服务实战项目，演示企业级微服务架构的四大核心组件：
> **Gateway 网关** → **OpenFeign 服务调用** → **负载均衡** → **熔断降级**。
> 通过"用户-订单"业务场景串联整个调用链路。

## 架构图

```
                         ┌─────────────────┐
                         │   Gateway :8080  │  ← 外部请求统一入口
                         └────────┬────────┘
                    ┌────────────┼────────────┐
                    ▼                         ▼
          ┌─────────────────┐     ┌─────────────────┐
          │  user-service   │     │  order-service  │
          │    :8081        │     │    :8082        │
          │                 │◄────│ OpenFeign 调用   │
          │  用户 CRUD      │     │  订单 CRUD       │
          └─────────────────┘     └─────────────────┘
```

## 模块一览

| 模块 | 技术栈 | 端口 | 核心功能 |
|------|--------|------|----------|
| [`common`](common) | Spring Boot 3.4.x | — | 公共 DTO、API 接口定义 |
| [`user-service`](user-service) | Spring Boot 3.4.x | 8081 | 用户 CRUD，提供 REST API |
| [`order-service`](order-service) | Spring Boot 3.4.x + OpenFeign | 8082 | 订单 CRUD，OpenFeign 调用用户服务 |
| [`gateway-service`](gateway-service) | Spring Cloud Gateway | 8080 | 统一入口、路由转发、过滤器、限流 |

## 快速启动

前置：JDK 21+、Maven 3.9+。

```bash
# 1. 编译全部模块
mvn clean compile

# 2. 启动三个终端，分别运行各服务
# 终端 1：用户服务
mvn -pl user-service spring-boot:run

# 终端 2：订单服务
mvn -pl order-service spring-boot:run

# 终端 3：网关
mvn -pl gateway-service spring-boot:run
```

## 验证调用链路

```bash
# 1. 通过网关查询用户（Gateway → user-service）
curl http://localhost:8080/api/users/1
# → {"code":200,"data":{"id":1,"username":"zhangsan","email":"zhangsan@example.com"}}

# 2. 创建订单（Gateway → order-service → OpenFeign → user-service）
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"iPad","amount":3999}'
# → {"code":200,"data":{"id":3,"userId":1,"productName":"iPad","amount":3999,"status":"CREATED"}}

# 3. 直接查订单
curl http://localhost:8080/api/orders/1
# → {"code":200,"data":{"id":1,"userId":1,"productName":"iPhone 16","amount":6999,"status":"CREATED"}}
```

## 教学知识点

| 编号 | 知识点 | 对应代码 |
|------|--------|----------|
| 01 | Spring Cloud Gateway 路由配置 | `gateway-service/src/main/resources/application.yml` |
| 02 | Gateway 过滤器（Header 注入） | `gateway-service/.../config/GatewayConfig.java` |
| 03 | OpenFeign 声明式 HTTP 客户端 | `order-service/.../client/UserFeignClient.java` |
| 04 | 服务间调用 + 异常处理 | `order-service/.../controller/OrderController.java` |
| 05 | 公共模块契约（DTO + API 接口） | `common/.../api/UserApi.java` |
| 06 | 统一响应体 Result | `common/.../dto/Result.java` |
| 07 | IP 限流 KeyResolver | `gateway-service/.../config/GatewayConfig.java` |

## 测试覆盖

本项目包含 **27 个单元测试**（JUnit 5 + AssertJ），覆盖核心业务逻辑：

| 模块 | 测试类 | 测试数 | 覆盖内容 |
|------|--------|--------|----------|
| `common` | `ResultTest` | 9 | 统一响应体工厂方法、泛型支持、独立实例 |
| `user-service` | `UserServiceTest` | 9 | 初始化数据、查询、创建、ID 递增、边界值 |
| `order-service` | `OrderServiceTest` | 9 | 初始化数据、查询、创建、金额精度、大数值 |

运行测试：

```bash
mvn test
```

## 实现边界

**已实现：**
- ✅ Spring Cloud Gateway 路由与过滤器
- ✅ OpenFeign 声明式服务调用
- ✅ 统一响应体 Result<T>
- ✅ 内存存储（ConcurrentHashMap）
- ✅ 27 个单元测试

**未实现（生产环境需补充）：**
- ❌ 服务注册与发现（Nacos/Eureka）
- ❌ 配置中心（Nacos/Apollo）
- ❌ 熔断降级（Resilience4j/Sentinel）
- ❌ 链路追踪（Micrometer Tracing/Zipkin）
- ❌ 持久化存储（MySQL/PostgreSQL）
- ❌ 分布式事务（Seata）
- ❌ 消息队列（Kafka/RocketMQ）
- ❌ 缓存（Redis）
- ❌ 认证授权（Spring Security + OAuth2）
- ❌ 容器化部署（Docker + Kubernetes）

## 目录结构

```
spring-cloud-micro-demo/
├── pom.xml                          # 聚合父工程
├── common/                          # 公共模块
│   └── src/main/java/com/xb/cloud/common/
│       ├── dto/UserDTO.java         # 用户 DTO
│       ├── dto/OrderDTO.java        # 订单 DTO
│       ├── dto/Result.java          # 统一响应
│       ├── api/UserApi.java         # 用户 API 接口
│       └── api/OrderApi.java        # 订单 API 接口
├── user-service/                    # 用户服务
│   └── src/main/java/com/xb/cloud/user/
│       ├── UserServiceApplication.java
│       ├── entity/User.java
│       ├── service/UserService.java
│       └── controller/UserController.java
├── order-service/                   # 订单服务
│   └── src/main/java/com/xb/cloud/order/
│       ├── OrderServiceApplication.java
│       ├── entity/Order.java
│       ├── service/OrderService.java
│       ├── client/UserFeignClient.java
│       └── controller/OrderController.java
├── gateway-service/                 # 网关服务
│   └── src/main/java/com/xb/cloud/gateway/
│       ├── GatewayApplication.java
│       └── config/GatewayConfig.java
└── docs/                            # 教学文档
```

## License

仅用于教学交流，作者：xb