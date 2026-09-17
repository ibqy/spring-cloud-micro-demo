package com.xb.cloud.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result 统一响应体测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p>验证统一响应体的工厂方法、状态码、数据结构。</p>
 */
@DisplayName("Result 统一响应体测试")
class ResultTest {

    @Test
    @DisplayName("ok() - 成功响应：code=200, message=success, data 不为 null")
    void ok_shouldReturn200WithSuccessMessageAndData() {
        // given
        String testData = "test data";

        // when
        Result<String> result = Result.ok(testData);

        // then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo(testData);
    }

    @Test
    @DisplayName("ok() - 支持复杂对象（UserDTO）")
    void ok_shouldSupportComplexObject() {
        // given
        UserDTO user = new UserDTO(1L, "zhangsan", "zhangsan@example.com");

        // when
        Result<UserDTO> result = Result.ok(user);

        // then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(1L);
        assertThat(result.getData().getUsername()).isEqualTo("zhangsan");
    }

    @Test
    @DisplayName("ok() - data 可以为 null")
    void ok_shouldAllowNullData() {
        // when
        Result<Object> result = Result.ok(null);

        // then
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("fail() - 失败响应：code=500, message 为指定错误信息, data=null")
    void fail_shouldReturn500WithErrorMessageAndNullData() {
        // given
        String errorMessage = "用户不存在";

        // when
        Result<Object> result = Result.fail(errorMessage);

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEqualTo(errorMessage);
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("fail() - 空字符串错误信息")
    void fail_shouldHandleEmptyErrorMessage() {
        // when
        Result<Object> result = Result.fail("");

        // then
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).isEmpty();
    }

    @Test
    @DisplayName("泛型支持 - Result<String>")
    void genericSupport_stringType() {
        // when
        Result<String> result = Result.ok("hello");

        // then
        assertThat(result.getData()).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("泛型支持 - Result<Integer>")
    void genericSupport_integerType() {
        // when
        Result<Integer> result = Result.ok(42);

        // then
        assertThat(result.getData()).isInstanceOf(Integer.class);
        assertThat(result.getData()).isEqualTo(42);
    }

    @Test
    @DisplayName("多次调用 ok() 返回独立实例")
    void multipleOkCalls_shouldReturnIndependentInstances() {
        // when
        Result<String> result1 = Result.ok("data1");
        Result<String> result2 = Result.ok("data2");

        // then
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1.getData()).isEqualTo("data1");
        assertThat(result2.getData()).isEqualTo("data2");
    }

    @Test
    @DisplayName("多次调用 fail() 返回独立实例")
    void multipleFailCalls_shouldReturnIndependentInstances() {
        // when
        Result<Object> result1 = Result.fail("error1");
        Result<Object> result2 = Result.fail("error2");

        // then
        assertThat(result1).isNotSameAs(result2);
        assertThat(result1.getMessage()).isEqualTo("error1");
        assertThat(result2.getMessage()).isEqualTo("error2");
    }
}
