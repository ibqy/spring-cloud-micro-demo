package com.xb.cloud.common.exception;

import com.xb.cloud.common.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 —— 微服务统一错误响应
 *
 * <p>作者：xb | 日期：2026-09-17</p>
 *
 * <p><b>教学知识点</b>：在微服务架构中，每个服务的异常响应格式必须统一，
 * 这样网关层（Gateway）和调用方（OpenFeign）才能用固定逻辑解析错误信息。
 * 把 {@code @RestControllerAdvice} 放在 common 模块，所有微服务自动继承。</p>
 *
 * <p>本项目的异常场景：</p>
 * <ul>
 *   <li>{@link IllegalArgumentException} — 查询不存在的用户/订单</li>
 *   <li>{@link MethodArgumentNotValidException} — {@code @Valid} 校验失败</li>
 *   <li>{@link MissingServletRequestParameterException} — GET 接口缺少必填参数</li>
 * </ul>
 *
 * @author ibqy
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数异常（如查询不存在的用户/订单）。
     *
     * <p>返回 400 状态码，让调用方知道是"请求参数有误"而非系统故障。</p>
     *
     * @param e 参数异常
     * @return 包含错误信息的 400 响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(e.getMessage()));
    }

    /**
     * 处理 {@code @Valid} 校验失败。
     *
     * <p>将所有字段的校验错误拼接成一条消息，方便前端定位问题。</p>
     *
     * @param e 校验异常
     * @return 包含所有校验错误的 400 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("校验失败: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(errors));
    }

    /**
     * 处理 GET 请求缺少必填参数的情况。
     *
     * @param e 缺参异常
     * @return 指出缺失参数名的 400 响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少参数: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail("缺少必填参数: " + e.getParameterName()));
    }

    /**
     * 兜底处理器：捕获所有未被上面匹配的异常。
     *
     * <p>生成唯一 requestId 便于排查日志，避免直接暴露异常堆栈给调用方。</p>
     *
     * @param e 未知异常
     * @return 包含 requestId 的 500 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception e) {
        // 生成短 requestId，方便在日志中快速定位对应的异常堆栈
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.error("未知异常 [{}]: {}", requestId, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("系统异常，requestId=" + requestId));
    }
}
