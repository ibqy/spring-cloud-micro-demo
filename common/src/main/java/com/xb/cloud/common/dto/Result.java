package com.xb.cloud.common.dto;

/**
 * 统一响应体 —— 所有 REST 接口的统一返回值
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：统一响应格式让前端和调用方以一致的方式处理结果。
 * {@code code + message + data} 是微服务间通信的常见约定。</p>
 *
 * @param <T> data 的类型
 */
public class Result<T> {

    /** 状态码：200 成功，其他表示异常 */
    private int code;
    /** 提示消息 */
    private String message;
    /** 响应数据 */
    private T data;

    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功响应 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 失败响应 */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}