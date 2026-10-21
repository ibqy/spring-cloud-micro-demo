# OpenFeign 与熔断降级

> 结论先行：服务间调用最危险的不是"调不通"，而是"调不通还一直等"。
> 熔断器的价值在于——**快速失败，把线程和连接还给系统**。

## 1. 契约先行：common 模块定义接口

微服务拆分的第一个问题：order-service 怎么知道 user-service 提供了什么？

本项目的答案是 **API 契约模块**：

```
common/
└── api/
    ├── UserApi.java    // GET /api/users/{id}
    └── OrderApi.java   // GET /api/orders/{id}、POST /api/orders
```

[UserApi](../common/src/main/java/com/xb/cloud/common/api/UserApi.java) 用 Spring MVC 注解描述契约：

```java
public interface UserApi {
    @GetMapping("/api/users/{id}")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);

    @PostMapping("/api/users")
    Result<UserDTO> createUser(@RequestBody UserDTO userDTO);
}
```

两侧各自实现/继承它：

| 角色 | 写法 | 作用 |
|:---|:---|:---|
| 服务端 | `UserController implements UserApi` | 保证实现与契约一致 |
| 消费端 | `UserFeignClient extends UserApi` | 自动获得调用能力；加熔断时覆盖方法，并重新声明 HTTP 映射 |

好处很直接：**契约改动会在编译期暴露**，而不是等上线后 404。

## 2. Feign 客户端：一行注解搞定远程调用

[UserFeignClient](../order-service/src/main/java/com/xb/cloud/order/client/UserFeignClient.java)：

```java
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserFeignClient extends UserApi {

    @Override
    @GetMapping("/api/users/{id}")          // 覆盖方法必须重新声明，注解不会继承
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    Result<UserDTO> getUserById(@PathVariable("id") Long id);
}
```

三个要点：

1. `name = "user-service"` —— 服务名，配合负载均衡时用它找实例
2. `url = "${user-service.url:...}"` —— 教学项目直连，同时支持配置覆盖；留空则走服务发现
3. `extends UserApi` —— 继承注解，不用重复写 `@GetMapping`

> `@Override` 只重写需要加熔断的方法。没加注解的方法仍然是普通 Feign 调用。
>
> 但覆盖时必须**重新声明** `@GetMapping` 和 `@PathVariable`：Spring MVC 契约只解析方法自身的注解，不会从 `UserApi` 继承。实测漏写会直接启动失败：
> `Method UserFeignClient#getUserById(Long) not annotated with HTTP method type`。

## 3. 熔断器：三个状态

Resilience4j 的 `CircuitBreaker` 是一个状态机：

```
       失败率超阈值
CLOSED ─────────────→ OPEN
  ↑                     │
  │  试探成功            │ 等待 wait-duration
  │                     ↓
  └──── HALF_OPEN ←─────┘
           │
           └─ 试探失败 → 回到 OPEN
```

| 状态 | 行为 |
|:---|:---|
| `CLOSED` | 正常放行，统计成功/失败 |
| `OPEN` | **直接拒绝**，不发起真实调用（快速失败） |
| `HALF_OPEN` | 放行少量试探请求，成功则恢复，失败则继续熔断 |

关键认知：`OPEN` 状态下**根本没有发出网络请求**，所以它比"超时重试"快几个数量级。

## 4. 配置：参数不是随便填的

`order-service/src/main/resources/application.yml`：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        sliding-window-type: COUNT_BASED      # 按调用次数统计（另一种是 TIME_BASED）
        sliding-window-size: 10               # 最近 10 次调用
        failure-rate-threshold: 50            # 失败率 ≥ 50% 触发熔断
        minimum-number-of-calls: 5            # 至少 5 次调用才计算失败率
        wait-duration-in-open-state: 30s      # 打开 30s 后进入半开
        permitted-number-of-calls-in-half-open-state: 5
        slow-call-duration-threshold: 2s      # 超过 2s 记为慢调用
        slow-call-rate-threshold: 40          # 慢调用占比 ≥ 40% 也触发熔断
  timelimiter:
    instances:
      userService:
        timeout-duration: 3s
```

逐个解释：

| 参数 | 为什么是这个值 |
|:---|:---|
| `sliding-window-size: 10` | 窗口太大反应慢，太小容易被抖动误判 |
| `minimum-number-of-calls: 5` | 防止"前 2 次失败就 100% 失败率"的误熔断 |
| `failure-rate-threshold: 50` | 一半失败说明下游确实有问题 |
| `wait-duration-in-open-state: 30s` | 给下游恢复时间，太短会反复打挂 |
| `slow-call-duration-threshold: 2s` | 慢调用也是故障，不能只统计异常 |
| `timeout-duration: 3s` | 超时要大于慢调用阈值，否则永远测不到慢调用 |

> `minimum-number-of-calls` 是最容易被忽略的参数。没有它，服务刚启动时前几次失败就会直接熔断。

## 5. 降级：返回什么比不返回更重要

[UserFallbackHandler](../order-service/src/main/java/com/xb/cloud/order/fallback/UserFallbackHandler.java) 只放降级逻辑；`fallbackMethod` 的入口必须声明在 Feign 接口上：

```java
// UserFeignClient 接口内：入口，代理类上可见
default Result<UserDTO> getUserByIdFallback(Long id, Throwable cause) {
    return UserFallbackHandler.getUserByIdFallback(id, cause);
}
```

```java
// UserFallbackHandler：纯逻辑，静态方法便于 default 方法委托
public static Result<UserDTO> getUserByIdFallback(Long userId, Throwable cause) {
    if (cause instanceof CallNotPermittedException) {
        return Result.fail("用户服务暂时不可用（熔断器已打开），请稍后重试");
    }
    return Result.fail("用户服务调用失败，已降级处理：" + cause.getMessage());
}
```

签名规则（必须严格遵守）：

```
原方法参数 + 一个 Throwable 参数 → 与原方法相同的返回类型
```

降级不是"吞掉异常"，而是**给出有语义的响应**：

| 降级策略 | 适用 | 示例 |
|:---|:---|:---|
| 返回兜底数据 | 查询类 | 用户信息返回缓存快照 |
| 返回明确错误 | 强一致场景 | "用户服务不可用，请稍后重试" |
| 走备用链路 | 有多通道 | 主库挂了读从库 |
| 排队稍后处理 | 写操作 | 落本地消息表，异步补偿 |

本项目用第二种——**创建订单必须校验用户存在，不能瞎猜**，所以宁可失败也不返回假数据。

### 实测记录：fallback 到底有没有被解析？

**结论：只写在独立 `@Component` 里不会被解析。** 2026-10-21 在本仓库真实跑过一遍：只启动 order-service（8082），让 user-service（8081）保持停止，连续调用 `POST /api/orders`。

| 现象 | 实测结果 |
|:---|:---|
| 首次失败 | `No fallback method match found` + `NoSuchMethodException` |
| HTTP 返回 | 500 默认错误体（不是降级文案） |
| 熔断器 | 照常累计失败，第 6 次进入 `OPEN`，但依旧 500 |

关键日志：

```
WARN  i.g.r.spring6.fallback.FallbackExecutor : No fallback method match found
java.lang.NoSuchMethodException: class com.xb.cloud.common.dto.Result
  class jdk.proxy2.$Proxy79.getUserByIdFallback(class java.lang.Long,class java.lang.Throwable)
```

原因：Feign 客户端是 JDK 动态代理（`jdk.proxy2.$ProxyXX`），Resilience4j 在目标对象的 class 上找 `fallbackMethod`；代理类只包含接口方法，独立 Bean 里的方法根本不在其中。

**已在仓库落地的修法**：把入口写成接口的 `default` 方法（逻辑仍委托给 `UserFallbackHandler`）。修完后同样的停服场景：

```
call 1~5: {"code":500,"message":"创建订单失败：用户服务调用失败，已降级处理：Connection refused..."}
call 6:   {"code":500,"message":"创建订单失败：用户服务暂时不可用（熔断器已打开），请稍后重试"}
```

状态迁移也验证了一遍（日志由 `CircuitBreakerEventLogger` 订阅事件打印，Resilience4j 默认不打印）：

```
11:02:58  CircuitBreaker 'userService' changed state from CLOSED to OPEN
11:03:28  CircuitBreaker 'userService' changed state from OPEN to HALF_OPEN
11:04:04  CircuitBreaker 'userService' changed state from HALF_OPEN to CLOSED
```

另一个修法是 `@FeignClient(fallback = UserFallback.class)` + `fallbackFactory`。它走的是 Spring Cloud CircuitBreaker 装饰器，需要额外依赖和 `feign.circuitbreaker.enabled=true`，熔断配置也不再复用 `@CircuitBreaker` 实例，教学项目没有采用。

> 另一个容易误判的地方：`OrderController` 曾把任何非 200 都改写成“用户不存在，无法创建订单”，降级文案被吞掉，看起来像“没降级”。现在改为透传下游 message，三种原因（用户不存在 / 调用失败 / 熔断打开）可以区分。

> 这类“注解看起来生效、实际没走到”的问题，只能靠**真实触发一次**来验证，不能靠读代码确认。

## 6. 超时、重试、熔断的关系

三者常被混为一谈：

| 机制 | 解决的问题 | 配错的表现 |
|:---|:---|:---|
| 超时 | 单次调用最多等多久 | 配太长 → 线程池耗尽 |
| 重试 | 偶发失败再来一次 | 配太多 → 放大下游压力 |
| 熔断 | 下游持续故障时快速失败 | 配太敏感 → 正常抖动也熔断 |

正确顺序是：**先超时，再熔断，重试要谨慎**。

因为重试会成倍放大流量：3 次重试 × 1000 QPS = 下游承受 3000 QPS，本来只是慢，重试之后直接挂。

## 7. 小结

- 契约放 `common`，服务端 `implements`，消费端 `extends`
- `@CircuitBreaker` 三状态：CLOSED / OPEN / HALF_OPEN
- `minimum-number-of-calls` 防误熔断，`slow-call-*` 让慢调用也计入故障
- 降级方法签名 = 原参数 + `Throwable`
- **fallback 入口必须在 Feign 接口上（default 方法）**，独立 Bean 里找不到
- 不要用兜底文案掩盖原因，透传 message 才能区分“不存在 / 失败 / 熔断”
- 熔断必须**真实触发验证**，不能只看注解

> 下一课：[05 · 启动与链路验证](/05-run-and-verify) —— 三个服务 + Redis，怎么按顺序拉起来并逐段验证。
