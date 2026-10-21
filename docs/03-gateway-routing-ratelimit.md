# 网关：路由、过滤器与限流

> 结论先行：网关不是一个"转发器"，它是微服务唯一的入口——
> **路由决定请求去哪，过滤器决定请求长什么样，限流决定请求能不能进。**

## 1. 三个核心概念

| 概念 | 作用 | 本项目中的体现 |
|:---|:---|:---|
| **Route（路由）** | 一条转发规则：id + 目标 URI + 断言 + 过滤器 | `/api/users/**` → `user-service:8081` |
| **Predicate（断言）** | 匹配条件，决定这条路由是否命中 | `Path=/api/users/**` |
| **Filter（过滤器）** | 在转发前后改写请求/响应 | `AddRequestHeader`、`RequestRateLimiter` |

请求进来后，Gateway 按顺序遍历路由，**第一条断言命中的路由生效**——所以路由顺序是有意义的，越具体的规则越要放前面。

## 2. 路由配置：YAML 还是 Java DSL

项目里两种都演示了：

- `gateway-service/src/main/resources/application.yml` —— 声明式，改配置不用重新编译
- [GatewayConfig](../gateway-service/src/main/java/com/xb/cloud/gateway/config/GatewayConfig.java) —— 编程式，适合需要动态计算路由的场景

YAML 版本：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - AddRequestHeader=X-Request-Source, gateway
```

`id` 必须全局唯一，它也是日志与指标里的路由标识。

> 教学项目用固定 `uri` 直连；生产通常写 `lb://user-service`，由 LoadBalancer 从注册中心解析实例。

## 3. 过滤器：在转发链路上做手脚

### 3.1 局部过滤器

只对某条路由生效：

```yaml
filters:
  - AddRequestHeader=X-Request-Source, gateway
```

下游服务因此能知道"这个请求来自网关"，而不是被外部直接调用。这类**来源标记**是零信任架构的起点。

### 3.2 全局过滤器

对所有路由生效：

```yaml
default-filters:
  - AddResponseHeader=X-Gateway-Version, 1.0
```

用途：统一打版本号、统一加 trace id、统一做响应脱敏。

### 3.3 过滤器执行顺序

`AddRequestHeader` 属于 `GatewayFilter`，在 `NettyRoutingFilter` 之前执行；`AddResponseHeader` 在响应阶段执行。多个过滤器按 `Ordered` 排序，数字越小越先执行。

> 踩坑：自定义过滤器如果没实现 `Ordered`，顺序不确定，可能出现"鉴权过滤器还没跑，转发已经发生了"。

## 4. 限流：`RequestRateLimiter`

```yaml
filters:
  - name: RequestRateLimiter
    args:
      key-resolver: "#{@ipKeyResolver}"
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

两个参数的区别要先讲清：

| 参数 | 含义 | 本项目取值 |
|:---|:---|:---|
| `replenishRate` | 令牌桶**每秒**补充多少令牌，即稳态 QPS | 10 |
| `burstCapacity` | 桶容量，即允许的瞬时突发 | 20 |

效果：稳态 10 QPS，最多容忍 20 个请求的瞬时尖峰。超出返回 `429 Too Many Requests`。

### 4.1 KeyResolver：限流按什么维度

[GatewayConfig](../gateway-service/src/main/java/com/xb/cloud/gateway/config/GatewayConfig.java) 只做了一件事——定义限流维度：

```java
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
}
```

| 维度 | 适用场景 | 风险 |
|:---|:---|:---|
| 按 IP | 匿名接口、防爬 | NAT 后大量用户共享 IP，会误伤 |
| 按用户 ID | 已登录接口 | 需要先解析 Token |
| 按 API Key | 开放平台 | 需要密钥管理 |
| 按路由 | 保护特定下游 | 粒度太粗 |

### 4.2 为什么必须依赖 Redis

`RequestRateLimiter` 默认用 **Redis + Lua 脚本**实现令牌桶，保证多网关实例共享同一个桶。

这意味着：**不启动 Redis，网关会启动失败或限流不生效**。默认配置连的是 `localhost:6379`。

> 这是本项目最容易卡住的一步。如果只想验证路由，可以先把 `RequestRateLimiter` 过滤器注释掉。

## 5. 网关该做与不该做的事

| 该做 | 不该做 |
|:---|:---|
| 路由转发、协议转换 | 业务逻辑编排 |
| 认证鉴权（Token 校验） | 复杂的数据聚合（用 BFF） |
| 限流、熔断、重试 | 长事务 |
| 请求/响应日志、trace id | 直接连数据库 |
| 灰度路由、A/B 分流 | 大量 CPU 密集计算 |

判断标准很简单：**网关是横切关注点的收口，不是业务逻辑的延伸**。

## 6. 常见坑

1. **路径没剥前缀**：`/api/users/1` 转发到下游还是 `/api/users/1`，如果下游 Controller 映射的是 `/users/1`，需要加 `StripPrefix=1`。
2. **CORS 重复配置**：网关和下游都配 CORS，浏览器会收到重复响应头而报错。只在网关配。
3. **超时没设**：Gateway 默认无超时，下游卡住会拖垮连接池。
4. **限流 key 取不到**：`getRemoteAddress()` 在代理后面拿到的是代理 IP，需要读 `X-Forwarded-For`。

## 7. 小结

- 路由 = id + uri + 断言 + 过滤器，顺序敏感
- 局部过滤器改单条路由，全局过滤器改所有响应
- 限流两个参数：`replenishRate`（稳态）+ `burstCapacity`（突发）
- `KeyResolver` 决定限流维度，选错会误伤
- 网关只做横切，不写业务

> 下一课：[04 · OpenFeign 与熔断降级](/04-feign-circuitbreaker) —— 请求进了 order-service 之后，它怎么安全地调用 user-service？
