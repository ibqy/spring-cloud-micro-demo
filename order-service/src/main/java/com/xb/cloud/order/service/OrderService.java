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
 *
 * <p><b>教学知识点</b>：Service 层封装业务逻辑，是 Controller 和"数据存储"之间的桥梁。
 * 本项目用 {@code ConcurrentHashMap} 模拟数据库，降低教学门槛。
 * 实际项目中，这里会被 {@code @Repository} / MyBatis / JPA 替代。</p>
 *
 * <p>关键设计：</p>
 * <ul>
 *   <li>{@code ConcurrentHashMap} —— 线程安全，支持多请求并发读写</li>
 *   <li>{@code AtomicLong} —— 原子自增 ID 生成器，比 {@code synchronized} 更高效</li>
 *   <li>{@code @PostConstruct} —— Bean 初始化完成后自动执行，用于加载演示数据</li>
 * </ul>
 *
 * @author ibqy
 */
@Service
public class OrderService {

    // 用 ConcurrentHashMap 模拟数据库，保证并发安全
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    // 原子 ID 生成器，线程安全且无需加锁
    private final AtomicLong idGen = new AtomicLong(1);

    /**
     * 初始化演示数据。
     *
     * <p>Spring 容器创建完 Bean 后自动调用，无需手动触发。
     * 预置两条数据方便启动后直接测试查询接口。</p>
     */
    @PostConstruct
    public void init() {
        save(1L, "iPhone 16", new BigDecimal("6999.00"));
        save(2L, "MacBook Pro", new BigDecimal("14999.00"));
    }

    /**
     * 根据 ID 查询订单
     *
     * @param id 订单 ID
     * @return 订单对象，不存在时返回 null
     */
    public Order findById(Long id) {
        return orders.get(id);
    }

    /**
     * 创建新订单
     *
     * <p>自动生成 ID，状态默认为 {@code CREATED}。</p>
     *
     * @param userId       下单用户 ID
     * @param productName  商品名称
     * @param amount       订单金额
     * @return 创建成功的订单对象
     */
    public Order create(Long userId, String productName, BigDecimal amount) {
        return save(userId, productName, amount);
    }

    private Order save(Long userId, String productName, BigDecimal amount) {
        Order o = new Order(idGen.getAndIncrement(), userId, productName, amount, "CREATED");
        orders.put(o.getId(), o);
        return o;
    }
}