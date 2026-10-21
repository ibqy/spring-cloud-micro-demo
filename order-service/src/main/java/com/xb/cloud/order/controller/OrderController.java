package com.xb.cloud.order.controller;

import com.xb.cloud.common.api.OrderApi;
import com.xb.cloud.common.dto.OrderDTO;
import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import com.xb.cloud.order.client.UserFeignClient;
import com.xb.cloud.order.entity.Order;
import com.xb.cloud.order.service.OrderService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单服务 Controller —— 实现 OrderApi 接口
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：创建订单时通过 {@link UserFeignClient#getUserById} 校验用户是否存在。
 * 这是微服务架构中最常见的"服务间调用"模式——订单服务依赖用户服务。</p>
 *
 * @author ibqy
 */
@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;
    private final UserFeignClient userFeignClient;

    public OrderController(OrderService orderService, UserFeignClient userFeignClient) {
        this.orderService = orderService;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public Result<OrderDTO> getOrderById(Long id) {
        Order order = orderService.findById(id);
        if (order == null) {
            return Result.fail("订单不存在，id=" + id);
        }
        return Result.ok(toDTO(order));
    }

    @Override
    public Result<OrderDTO> createOrder(OrderDTO orderDTO) {
        // 调用 user-service 校验用户是否存在
        Result<UserDTO> userResult = userFeignClient.getUserById(orderDTO.getUserId());
        if (userResult.getCode() != 200) {
            // 不揉平下游语义：用户不存在、调用失败降级、熔断器打开是三种不同原因，
            // 直接透传 message 才能在调用方看到真实原因
            return Result.fail("创建订单失败：" + userResult.getMessage());
        }
        Order order = orderService.create(
                orderDTO.getUserId(),
                orderDTO.getProductName(),
                orderDTO.getAmount());
        return Result.ok(toDTO(order));
    }

    private OrderDTO toDTO(Order order) {
        return new OrderDTO(
                order.getId(), order.getUserId(),
                order.getProductName(), order.getAmount(),
                order.getStatus());
    }
}