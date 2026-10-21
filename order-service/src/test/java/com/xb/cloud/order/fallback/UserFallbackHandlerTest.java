package com.xb.cloud.order.fallback;

import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import com.xb.cloud.order.client.UserFeignClient;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * UserFallbackHandler 降级处理器测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p>验证熔断降级的核心逻辑：不同异常类型返回不同的降级消息。</p>
 */
@DisplayName("UserFallbackHandler 降级处理器测试")
class UserFallbackHandlerTest {

    @Test
    @DisplayName("熔断器打开时 - 返回'熔断器已打开'提示")
    void getUserByIdFallback_whenCircuitBreakerOpen_shouldReturnCircuitBreakerMessage() {
        // given
        Long userId = 1L;
        Throwable cause = mock(CallNotPermittedException.class);

        // when
        Result<UserDTO> result = UserFallbackHandler.getUserByIdFallback(userId, cause);

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("熔断器已打开");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("超时异常时 - 返回超时相关信息")
    void getUserByIdFallback_whenTimeout_shouldReturnTimeoutMessage() {
        // given
        Long userId = 2L;
        Throwable cause = new RuntimeException("Read timed out");

        // when
        Result<UserDTO> result = UserFallbackHandler.getUserByIdFallback(userId, cause);

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已降级处理");
        assertThat(result.getMessage()).contains("Read timed out");
    }

    @Test
    @DisplayName("网络不可达时 - 返回网络异常信息")
    void getUserByIdFallback_whenNetworkUnreachable_shouldReturnNetworkMessage() {
        // given
        Long userId = 3L;
        Throwable cause = new RuntimeException("Connection refused");

        // when
        Result<UserDTO> result = UserFallbackHandler.getUserByIdFallback(userId, cause);

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已降级处理");
        assertThat(result.getMessage()).contains("Connection refused");
    }

    @Test
    @DisplayName("降级响应 - data 始终为 null")
    void getUserByIdFallback_shouldAlwaysReturnNullData() {
        // given
        Long userId = 999L;
        Throwable cause = new RuntimeException("Any error");

        // when
        Result<UserDTO> result = UserFallbackHandler.getUserByIdFallback(userId, cause);

        // then
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("降级响应 - code 始终为 500")
    void getUserByIdFallback_shouldAlwaysReturn500Code() {
        // given
        Long userId = 1L;
        Throwable cause = new RuntimeException("Error");

        // when
        Result<UserDTO> result = UserFallbackHandler.getUserByIdFallback(userId, cause);

        // then
        assertThat(result.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("接口 default 降级入口 - 可被 Feign 代理找到并正确委托")
    void userFeignClientDefaultFallback_shouldDelegateToHandler() {
        // given：Feign 客户端本质上就是该接口的代理，default 方法在代理类上可见
        UserFeignClient client = new UserFeignClient() {
            @Override
            public Result<UserDTO> getUserById(Long id) {
                throw new UnsupportedOperationException("本用例不发起真实调用");
            }

            @Override
            public Result<UserDTO> createUser(UserDTO userDTO) {
                throw new UnsupportedOperationException("本用例不发起真实调用");
            }
        };

        // when
        Result<UserDTO> result = client.getUserByIdFallback(7L, new RuntimeException("Connection refused"));

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("已降级处理").contains("Connection refused");
    }
}
