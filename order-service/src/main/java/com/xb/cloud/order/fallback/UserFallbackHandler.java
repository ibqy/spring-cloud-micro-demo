package com.xb.cloud.order.fallback;

import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户服务调用降级处理器 —— Resilience4j Fallback
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p><b>教学知识点</b>：当 user-service 不可用（超时、异常、熔断器打开）时，
 * Resilience4j 自动调用 fallbackMethod 指定的方法，返回降级响应而非抛出异常。
 * 这是微服务"优雅降级"的核心模式——宁可返回有限功能的响应，也不能让整个调用链崩溃。</p>
 *
 * <p><b>三种触发降级的场景</b>：</p>
 * <ol>
 *   <li><b>超时</b>：user-service 响应时间超过配置的 timeoutDuration</li>
 *   <li><b>异常</b>：user-service 返回 5xx 或网络不可达</li>
 *   <li><b>熔断器打开</b>：连续失败次数达到阈值，熔断器进入 OPEN 状态</li>
 * </ol>
 *
 * <p><b>注意</b>：这个类只负责“降级逻辑”，不能直接作为 {@code fallbackMethod} 的目标。
 * Feign 客户端是 JDK 动态代理，Resilience4j 只在代理类上找 fallback 方法，
 * 独立 Bean 里的方法找不到（实测抛出 {@code NoSuchMethodException}）。
 * 因此入口是 {@link com.xb.cloud.order.client.UserFeignClient#getUserByIdFallback}，
 * 再由它委托到本类。详见 docs/04-feign-circuitbreaker.md 的实测记录。</p>
 *
 * @author ibqy
 */
public final class UserFallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(UserFallbackHandler.class);

    /**
     * getUserById 的降级方法。
     *
     * <p>方法签名必须与原方法完全一致（参数类型 + 返回类型），
     * 额外多一个 Throwable 参数接收原始异常。</p>
     *
     * @param userId 用户 ID（原方法参数）
     * @param cause 触发降级的异常（超时/熔断/网络异常）
     * @return 降级后的失败响应
     */
    public static Result<UserDTO> getUserByIdFallback(Long userId, Throwable cause) {
        log.warn("[Fallback] user-service 调用失败，userId={}，原因：{}",
                userId, cause.getMessage());

        if (cause instanceof CallNotPermittedException) {
            return Result.fail("用户服务暂时不可用（熔断器已打开），请稍后重试");
        }

        return Result.fail("用户服务调用失败，已降级处理：" + cause.getMessage());
    }
}
