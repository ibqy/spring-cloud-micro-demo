# 启动与链路验证

> 结论先行：微服务项目最容易出现的不是"代码错"，而是"启动顺序错、依赖没起、验证不完整"。
> 这一课给出**可以照抄的启动顺序**和**逐段验证的 curl**。

## 1. 端口与依赖

| 服务 | 端口 | 依赖 |
|:---|:---|:---|
| `user-service` | 8081 | 无 |
| `order-service` | 8082 | user-service（Feign 调用） |
| `gateway-service` | 8080 | user-service、order-service，**Redis**（限流） |

Redis 是网关限流的硬依赖，默认连 `localhost:6379`：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

> 只想验证路由不想装 Redis？把 `gateway-service` 里 `RequestRateLimiter` 过滤器注释掉即可，其余功能不受影响。

## 2. 启动顺序

```bash
# 0. 首次构建：先把 common 装进本地仓库（user-service / order-service 都依赖它）
mvn -pl common install -DskipTests

# 1. 先起被调用方
mvn -pl user-service spring-boot:run

# 2. 再起调用方
mvn -pl order-service spring-boot:run

# 3. 最后起网关
mvn -pl gateway-service spring-boot:run
```

顺序的理由：`order-service` 启动时 Feign 客户端会初始化，如果此时 user-service 不在，虽然不会启动失败，但第一次调用会直接走失败路径；先起被调用方可以避免"一启动就熔断"的困惑。

> 不要写成 `mvn -pl order-service -am spring-boot:run`：`-am` 会把聚合父 pom 也拉进 reactor，而父 pom 没有声明 `spring-boot-maven-plugin`，实测会报 `No plugin found for prefix 'spring-boot'`。先装 `common`，再按模块启动即可。

## 3. 分段验证

### 3.1 直连 user-service

```bash
curl http://localhost:8081/api/users/1
```

预期：

```json
{"code":200,"message":"success","data":{"id":1,"username":"zhangsan","email":"zhangsan@example.com"}}
```

预置数据是 `zhangsan`(1) 和 `lisi`(2)。

### 3.2 直连 order-service

```bash
curl http://localhost:8082/api/orders/1
```

### 3.3 验证 Feign 调用

创建订单会先调 user-service 校验用户：

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productName":"iPad","amount":4999.00}'
```

用不存在的用户应该失败：

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":999,"productName":"iPad","amount":4999.00}'
# 预期：{"code":500,"message":"创建订单失败：用户不存在，id=999",...}
```

### 3.4 验证网关转发

```bash
curl -i http://localhost:8080/api/users/1
```

检查响应头里有没有全局过滤器加的标记：

```
X-Gateway-Version: 1.0
```

### 3.5 验证来源头透传

在 user-service 里临时打印 `X-Request-Source`，通过网关访问时应为 `gateway`，直连时为空。

这是验证"请求确实经过网关"最直接的方式——**不要只看返回 200 就以为链路对了**。

## 4. 主动触发熔断

这是本课最重要的动作：**亲手把熔断器打开一次**。

### 步骤

1. 正常调用几次，确认链路通
2. **停掉 user-service**（Ctrl+C）
3. 连续调用创建订单 6 次以上（`minimum-number-of-calls` 是 5）

```bash
for ($i=1; $i -le 8; $i++) {
  curl -s -X POST http://localhost:8082/api/orders `
    -H "Content-Type: application/json" `
    -d '{"userId":1,"productName":"test","amount":1.00}'
  Write-Output ""
}
```

> PowerShell 5.1 直接把带双引号的 JSON 传给 `curl.exe -d` 会被吞引号（实测报 400）。改用 `--data-binary "@body.json"` 或 `Invoke-RestMethod` + `ConvertTo-Json` 更稳。

### 2026-10-21 实测结果

| 调用 | 返回 |
|:---|:---|
| 1~5 | `{"code":500,"message":"创建订单失败：用户服务调用失败，已降级处理：Connection refused..."}` |
| 6+ | `{"code":500,"message":"创建订单失败：用户服务暂时不可用（熔断器已打开），请稍后重试"}` |

注意：HTTP 状态始终是 200，业务状态在 `code` 字段（统一响应体 `Result`）。

日志里能看到状态变化：

```
INFO  CircuitBreakerEventLogger : CircuitBreaker 'userService' changed state from CLOSED to OPEN
WARN  UserFallbackHandler : [Fallback] user-service 调用失败，userId=1，原因：CircuitBreaker 'userService' is OPEN and does not permit further calls
```

> Resilience4j 2.x 默认只发布事件、不打印状态迁移；上面的 INFO 日志由 `CircuitBreakerEventLogger` 订阅后输出，否则只看得到 fallback 的 WARN。

### 观察点

| 阶段 | 现象 | 说明 |
|:---|:---|:---|
| 前 5 次 | 几十毫秒内返回 `Connection refused` 降级文案 | 失败率在累积 |
| 第 6 次起 | 立即返回“熔断器已打开”，**不再发起连接** | 熔断器已 `OPEN` |
| 看 fallback 日志 | 原因从 `Connection refused` 变成 `CircuitBreaker ... is OPEN` | 证明请求根本没发出去 |

### 恢复验证

重启 user-service，等 `wait-duration-in-open-state: 30s` 之后，连续调用 5 次（`permitted-number-of-calls-in-half-open-state`）：

```
11:03:28  CircuitBreaker 'userService' changed state from OPEN to HALF_OPEN
11:04:04  CircuitBreaker 'userService' changed state from HALF_OPEN to CLOSED
```

调用恢复正常（`code:200`）。注意 HALF_OPEN 不是“一次成功就关闭”，要凑满 5 次探测。

> 如果停掉 user-service 后返回 500 默认错误体而不是降级文案，说明 fallback 没有被解析——见 [04 · OpenFeign 与熔断降级](/04-feign-circuitbreaker) 最后一节的实测记录。

## 5. 验证限流

```bash
# 连续发 30 个请求，观察 429
for ($i=1; $i -le 30; $i++) {
  curl -s -o NUL -w "%{http_code} " http://localhost:8080/api/users/1
}
```

预期：前 20 个左右返回 `200`（burstCapacity=20），之后出现 `429`，然后按每秒 10 个的速率恢复。

> 限流是**令牌桶**而不是固定窗口，所以不会出现"整分钟卡死"的现象。

## 6. 常见启动问题

| 现象 | 原因 | 处理 |
|:---|:---|:---|
| 网关启动报 Redis 连接失败 | 没装/没启动 Redis | 启动 Redis，或注释限流过滤器 |
| `Could not resolve placeholder 'user-service.url'` | 配置缺失 | `order-service/application.yml` 里补 `user-service.url` |
| 端口被占用 | 上次进程没退 | `netstat -ano \| findstr :8080` 后 kill |
| `No plugin found for prefix 'spring-boot'` | 用了 `-pl ... -am`，聚合父 pom 也被执行 | 先 `mvn -pl common install -DskipTests`，再按模块启动 |
| 通过网关 404，直连正常 | 路由断言不匹配 | 检查路径是否以 `/api/users` 开头 |
| Feign 报 `Load balancer does not contain an instance` | 用了服务名但没注册中心 | 教学项目用 `url` 直连，确认 `url` 生效 |
| 限流不生效 | 没引入 Redis 依赖或 key 解析为 null | 检查 `spring-boot-starter-data-redis-reactive` 与 `KeyResolver` |

## 7. 本项目的教学边界

清楚"没做什么"，比知道"做了什么"更重要：

| 未实现 | 生产替代方案 |
|:---|:---|
| 注册中心 | Nacos / Eureka / Consul（`uri: lb://user-service`） |
| 配置中心 | Nacos Config / Spring Cloud Config |
| 链路追踪 | Micrometer Tracing + Zipkin / SkyWalking |
| 分布式事务 | Seata / 本地消息表 + 补偿 |
| 持久化 | MySQL + MyBatis-Plus / JPA |
| 认证鉴权 | Spring Security + JWT（可参考 [pay-teaching-demo](https://ibqy.github.io/pay-teaching-demo/) 的幂等与安全实践） |

## 8. 小结

- 启动顺序：被调用方 → 调用方 → 网关
- 验证顺序：直连 → Feign → 网关 → 熔断 → 限流
- **熔断必须真实触发**，观察 `CLOSED → OPEN → HALF_OPEN → CLOSED` 的日志
- 限流依赖 Redis，且是令牌桶不是固定窗口
- 明确教学边界，才知道往生产走还差什么

> 回到课程起点：[01 · 整体架构](/01-architecture) ｜ 上一课：[04 · OpenFeign 与熔断降级](/04-feign-circuitbreaker)
