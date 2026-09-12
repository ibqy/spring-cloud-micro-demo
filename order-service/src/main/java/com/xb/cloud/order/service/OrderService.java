package com.xb.cloud.order.service;

import com.xb.cloud.order.entity.Order;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单服务 —— 内存实现
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 */
@Service
public class OrderService {

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void init() {
        save(1L, "iPhone 16", new BigDecimal("6999.00"));
        save(2L, "MacBook Pro", new BigDecimal("14999.00"));
    }

    /** 根据 ID 查询订单 */
    public Order findById(Long id) {
        return orders.get(id);
    }

    /** 创建订单 */
    public Order create(Long userId, String productName, BigDecimal amount) {
        return save(userId, productName, amount);
    }

    private Order save(Long userId, String productName, BigDecimal amount) {
        Order o = new Order(idGen.getAndIncrement(), userId, productName, amount, "CREATED");
        orders.put(o.getId(), o);
        return o;
    }
}