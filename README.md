<p align="center">
  <a href="https://ibqy.github.io/spring-cloud-micro-demo/"><img src="https://img.shields.io/badge/在线课件-2F6FEB?style=flat-square" alt="在线课件"></a>
  <img src="https://img.shields.io/badge/Java-21-2266EE?style=flat-square" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?style=flat-square" alt="Spring Boot 3.5.16">
  <img src="https://img.shields.io/badge/Spring_Cloud-2025.0.3-6DB33F?style=flat-square" alt="Spring Cloud 2025.0.3">
  <img src="https://img.shields.io/github/license/ibqy/spring-cloud-micro-demo?style=flat-square" alt="License">
  <img src="https://img.shields.io/github/stars/ibqy/spring-cloud-micro-demo?style=flat-square" alt="Stars">
</p>

> **课程位置** 阶段 1 · 架构与资金安全 — 第 2 站
> [上一站 DDD 架构](https://ibqy.github.io/ddd-arch-demo/) → **Spring Cloud 微服务** → [下一站 Spring Boot 高阶](https://ibqy.github.io/spring-advanced-demo/) ｜ [完整学习路线](https://ibqy.github.io/curriculum)

---

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
          ┌─────────────────┐     ┌─────────────────────────┐
          │  user-service   │     │     order-service :8082  │
          │    :8081        │     │                         │
          │                 │◄────│ OpenFeign + 熔断降级     │
          │  用户 CRUD      │     │ @CircuitBreaker          │
          └─────────────────┘     │ Fallback 降级处理        │
                                  └─────────────────────────┘
```

## 模块一览

| 模块 | 技术栈 | 端口 | 核心功能 |
|------|--------|------|----------|
| [`common`](common) | Spring Boot 3.5.16 | — | 公共 DTO、API 接口定义 |
| [`user-service`](user-service) | Spring Boot 3.5.16 | 8081 | 用户 CRUD，提供 REST API |
| [`order-service`](order-service) | Spring Boot 3.5.16 + OpenFeign + Resilience4j | 8082 | 订单 CRUD，OpenFeign 调用用户服务，熔断降级 |
| [`gateway-service`](gateway-service) | Spring Cloud Gateway | 8080 | 统一入口、路由转发、过滤器、限流 |

## 快速启动

前置：JDK 21+、Maven 3.6.3+。

```bash
# 1. 编译并把全部模块装进本地仓库（user/order/gateway 都依赖 common）
mvn clean install -DskipTests

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
| 08 | **Resilience4j 熔断器** | `order-service/.../client/UserFeignClient.java` |
| 09 | **Fallback 降级处理** | `order-service/.../client/UserFeignClient.java`（default 入口）+ `fallback/UserFallbackHandler.java`（降级逻辑） |
| 10 | **熔断器配置（滑动窗口/阈值/半开）** | `order-service/src/main/resources/application.yml` |
| 11 | **全局异常处理** | `common/.../exception/GlobalExceptionHandler.java` |

## 高阶实战：熔断降级

### 为什么需要熔断？

微服务架构中，order-service 通过 HTTP 调用 user-service。如果 user-service 响应变慢或不可用，
order-service 的线程会被大量占用等待响应，最终导致**雪崩效应**——一个服务拖垮整条调用链。

熔断器（Circuit Breaker）的核心思想：**当错误率达到阈值时，快速失败，不再等待**。

### 三种状态

```
CLOSED（正常） ──失败率≥50%──▶ OPEN（熔断）
      ▲                           │
      │                     等待 30 秒
      │                           ▼
      └──── 探测 5 次均成功 ── HALF_OPEN（探测）
```

| 状态 | 行为 | 触发条件 |
|------|------|----------|
| **CLOSED** | 正常放行请求 | 初始状态 / 探测成功 |
| **OPEN** | 直接走 Fallback，不调用远程服务 | 失败率 ≥ 50%（最近 10 次、至少 5 次调用） |
| **HALF_OPEN** | 放行少量请求探测恢复情况 | OPEN 状态等待 30 秒后自动进入 |

### 核心配置解读

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:                          # 对应 @CircuitBreaker(name = "userService")
        sliding-window-type: COUNT_BASED    # 基于请求次数（而非时间）的滑动窗口
        sliding-window-size: 10             # 窗口大小：最近 10 次调用
        failure-rate-threshold: 50          # 失败率 ≥ 50% 时打开熔断器
        wait-duration-in-open-state: 30s    # OPEN 状态持续 30 秒后进入 HALF_OPEN
        permitted-number-of-calls-in-half-open-state: 5  # 探测时放行 5 个请求
        slow-call-duration-threshold: 2s    # 响应 > 2 秒视为慢调用
        slow-call-rate-threshold: 40        # 慢调用占比 ≥ 40% 也触发熔断
```

### Fallback 降级处理

`UserFallbackHandler` 只负责降级逻辑，`fallbackMethod` 的入口必须写在 Feign 接口上（`default` 方法）——
Feign 客户端是 JDK 动态代理，Resilience4j 只在代理类上找降级方法：

```java
public interface UserFeignClient extends UserApi {

    @Override
    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    // 必须是接口 default 方法，代理类才找得到
    default Result<UserDTO> getUserByIdFallback(Long id, Throwable cause) {
        return UserFallbackHandler.getUserByIdFallback(id, cause);
    }
}
```

**三种触发降级的场景**：
1. **熔断器打开**：`CallNotPermittedException` → 快速失败，不浪费资源
2. **超时异常**：user-service 响应过慢 → 返回降级响应
3. **网络异常**：user-service 不可达 → 返回降级响应

> **实测结论（2026-10-21）**：停掉 user-service 连续调用，前 5 次返回“已降级处理：Connection refused”，第 6 次起返回“熔断器已打开”；30s 后 `OPEN → HALF_OPEN`，再连续 5 次成功调用后 `HALF_OPEN → CLOSED`。
> 完整日志、失败原因与两种修法见 [docs/04-feign-circuitbreaker.md](docs/04-feign-circuitbreaker.md) 的“实测记录”。

## 测试覆盖

本项目包含 **36 个单元测试**（JUnit 5 + AssertJ + Mockito），覆盖核心业务逻辑与熔断降级：

| 模块 | 测试类 | 测试数 | 覆盖内容 |
|------|--------|--------|----------|
| `common` | `ResultTest` | 9 | 统一响应体工厂方法、泛型支持、独立实例 |
| `common` | `GlobalExceptionHandlerTest` | 3 | 参数异常、缺少参数、未知异常 |
| `user-service` | `UserServiceTest` | 8 | 初始化数据、查询、创建、ID 递增、边界值 |
| `order-service` | `OrderServiceTest` | 10 | 初始化数据、查询、创建、金额精度、大数值 |
| `order-service` | `UserFallbackHandlerTest` | 6 | 熔断器打开降级、超时降级、网络异常降级、null data、500 code、接口 default 入口委托 |

运行测试：

```bash
mvn test
```

## 实现边界

**已实现：**
- ✅ Spring Cloud Gateway 路由与过滤器
- ✅ OpenFeign 声明式服务调用
- ✅ Resilience4j 熔断降级（@CircuitBreaker + 接口 default fallback）
- ✅ 统一响应体 Result<T>
- ✅ 全局异常处理 GlobalExceptionHandler（@RestControllerAdvice）
- ✅ 内存存储（ConcurrentHashMap）
- ✅ 36 个单元测试

**未实现（生产环境需补充）：**
- ❌ 服务注册与发现（Nacos/Eureka）
- ❌ 配置中心（Nacos/Apollo）
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
│       ├── api/OrderApi.java        # 订单 API 接口
│       └── exception/GlobalExceptionHandler.java  # 全局异常处理
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
│       ├── client/UserFeignClient.java    # @CircuitBreaker + default fallback
│       ├── fallback/UserFallbackHandler.java  # 降级逻辑
│       ├── config/CircuitBreakerEventLogger.java  # 状态迁移日志
│       └── controller/OrderController.java
├── gateway-service/                 # 网关服务
│   └── src/main/java/com/xb/cloud/gateway/
│       ├── GatewayApplication.java
│       └── config/GatewayConfig.java
└── docs/                            # 教学文档
```

## License

仅用于教学交流，作者：xb