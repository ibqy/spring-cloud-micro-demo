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
| [`common`](common) | Spring Boot 3.4.x | — | 公共 DTO、API 接口定义 |
| [`user-service`](user-service) | Spring Boot 3.4.x | 8081 | 用户 CRUD，提供 REST API |
| [`order-service`](order-service) | Spring Boot 3.4.x + OpenFeign + Resilience4j | 8082 | 订单 CRUD，OpenFeign 调用用户服务，熔断降级 |
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
| 08 | **Resilience4j 熔断器** | `order-service/.../client/UserFeignClient.java` |
| 09 | **Fallback 降级处理** | `order-service/.../fallback/UserFallbackHandler.java` |
| 10 | **熔断器配置（滑动窗口/阈值/半开）** | `order-service/src/main/resources/application.yml` |

## 高阶实战：熔断降级

### 为什么需要熔断？

微服务架构中，order-service 通过 HTTP 调用 user-service。如果 user-service 响应变慢或不可用，
order-service 的线程会被大量占用等待响应，最终导致**雪崩效应**——一个服务拖垮整条调用链。

熔断器（Circuit Breaker）的核心思想：**当错误率达到阈值时，快速失败，不再等待**。

### 三种状态

```
CLOSED（正常） ──失败率≥50%──▶ OPEN（熔断）
      ▲                           │
      │                     等待 10 秒
      │                           ▼
      └──── 成功率≥80% ── HALF_OPEN（探测）
```

| 状态 | 行为 | 触发条件 |
|------|------|----------|
| **CLOSED** | 正常放行请求 | 初始状态 / 探测成功 |
| **OPEN** | 直接走 Fallback，不调用远程服务 | 失败率 ≥ 50%（滑动窗口 10 次） |
| **HALF_OPEN** | 放行少量请求探测恢复情况 | OPEN 状态等待 10 秒后自动进入 |

### 核心配置解读

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:                          # 对应 @CircuitBreaker(name = "userService")
        sliding-window-type: COUNT_BASED    # 基于请求次数（而非时间）的滑动窗口
        sliding-window-size: 10             # 窗口大小：最近 10 次调用
        failure-rate-threshold: 50          # 失败率 ≥ 50% 时打开熔断器
        wait-duration-in-open-state: 10s    # OPEN 状态持续 10 秒后进入 HALF_OPEN
        permitted-number-of-calls-in-half-open-state: 3  # 探测时放行 3 个请求
        slow-call-duration-threshold: 2s    # 响应 > 2 秒视为慢调用
        slow-call-rate-threshold: 80        # 慢调用占比 ≥ 80% 也触发熔断
```

### Fallback 降级处理

`UserFallbackHandler` 是降级处理器，当熔断器打开或服务调用失败时自动触发：

```java
@Component
public class UserFallbackHandler {
    public Result<UserDTO> getUserByIdFallback(Long userId, Throwable cause) {
        if (cause instanceof CallNotPermittedException) {
            return Result.fail("用户服务暂时不可用（熔断器已打开）");
        }
        return Result.fail("用户服务调用失败，已降级处理：" + cause.getMessage());
    }
}
```

**三种触发降级的场景**：
1. **熔断器打开**：`CallNotPermittedException` → 快速失败，不浪费资源
2. **超时异常**：user-service 响应过慢 → 返回降级响应
3. **网络异常**：user-service 不可达 → 返回降级响应

## 测试覆盖

本项目包含 **32 个单元测试**（JUnit 5 + AssertJ + Mockito），覆盖核心业务逻辑与熔断降级：

| 模块 | 测试类 | 测试数 | 覆盖内容 |
|------|--------|--------|----------|
| `common` | `ResultTest` | 9 | 统一响应体工厂方法、泛型支持、独立实例 |
| `user-service` | `UserServiceTest` | 8 | 初始化数据、查询、创建、ID 递增、边界值 |
| `order-service` | `OrderServiceTest` | 10 | 初始化数据、查询、创建、金额精度、大数值 |
| `order-service` | `UserFallbackHandlerTest` | 5 | 熔断器打开降级、超时降级、网络异常降级、null data、500 code |

运行测试：

```bash
mvn test
```

## 实现边界

**已实现：**
- ✅ Spring Cloud Gateway 路由与过滤器
- ✅ OpenFeign 声明式服务调用
- ✅ Resilience4j 熔断降级（@CircuitBreaker + Fallback）
- ✅ 统一响应体 Result<T>
- ✅ 内存存储（ConcurrentHashMap）
- ✅ 32 个单元测试

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
│       ├── client/UserFeignClient.java    # @CircuitBreaker 熔断保护
│       ├── fallback/UserFallbackHandler.java  # 降级处理
│       └── controller/OrderController.java
├── gateway-service/                 # 网关服务
│   └── src/main/java/com/xb/cloud/gateway/
│       ├── GatewayApplication.java
│       └── config/GatewayConfig.java
└── docs/                            # 教学文档
```

## License

仅用于教学交流，作者：xb