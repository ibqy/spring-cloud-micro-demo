package com.xb.cloud.common.exception;

import com.xb.cloud.common.dto.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试
 *
 * <p>作者：xb | 日期：2026-09-17</p>
 */
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("异常处理")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("IllegalArgumentException → Result.fail(400)")
        void handleIllegalArgument() {
            ResponseEntity<Result<Void>> resp =
                    handler.handleIllegalArgument(new IllegalArgumentException("用户不存在，id=999"));
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertEquals(500, resp.getBody().getCode());
            assertTrue(resp.getBody().getMessage().contains("用户不存在"));
        }

        @Test
        @DisplayName("未知异常 → Result.fail(500) + requestId")
        void handleUnknown() {
            ResponseEntity<Result<Void>> resp =
                    handler.handleUnknown(new RuntimeException("unexpected"));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
            assertEquals(500, resp.getBody().getCode());
            assertTrue(resp.getBody().getMessage().contains("requestId="));
        }

        @Test
        @DisplayName("缺少必填参数 → Result.fail(400)")
        void handleMissingParam() {
            MissingServletRequestParameterException ex =
                    new MissingServletRequestParameterException("id", "pathVariable");
            ResponseEntity<Result<Void>> resp = handler.handleMissingParam(ex);
            assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
            assertTrue(resp.getBody().getMessage().contains("id"));
        }
    }
}
