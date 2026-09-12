package com.xb.cloud.common.api;

import com.xb.cloud.common.dto.OrderDTO;
import com.xb.cloud.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 API 接口 —— 公共定义
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：与 UserApi 同理，OrderApi 定义了订单服务的契约。
 * gateway 和 order-service 内部都依赖此接口保持一致性。</p>
 */
public interface OrderApi {

    /** 根据 ID 查询订单 */
    @GetMapping("/api/orders/{id}")
    Result<OrderDTO> getOrderById(@PathVariable Long id);

    /** 创建订单 */
    @PostMapping("/api/orders")
    Result<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO);
}