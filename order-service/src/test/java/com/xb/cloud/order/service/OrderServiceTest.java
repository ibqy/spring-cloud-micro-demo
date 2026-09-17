package com.xb.cloud.order.service;

import com.xb.cloud.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderService 单元测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p>验证订单服务的核心业务逻辑：初始化数据、查询、创建、金额精度。</p>
 */
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
        orderService.init();
    }

    @Test
    @DisplayName("init() - 初始化后应有 2 条演示订单")
    void init_shouldCreateTwoDemoOrders() {
        // when
        Order order1 = orderService.findById(1L);
        Order order2 = orderService.findById(2L);

        // then
        assertThat(order1).isNotNull();
        assertThat(order1.getUserId()).isEqualTo(1L);
        assertThat(order1.getProductName()).isEqualTo("iPhone 16");
        assertThat(order1.getAmount()).isEqualByComparingTo(new BigDecimal("6999.00"));
        assertThat(order1.getStatus()).isEqualTo("CREATED");

        assertThat(order2).isNotNull();
        assertThat(order2.getUserId()).isEqualTo(2L);
        assertThat(order2.getProductName()).isEqualTo("MacBook Pro");
        assertThat(order2.getAmount()).isEqualByComparingTo(new BigDecimal("14999.00"));
    }

    @Test
    @DisplayName("findById() - 查询存在的订单")
    void findById_existingOrder_shouldReturnOrder() {
        // when
        Order order = orderService.findById(1L);

        // then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getProductName()).isEqualTo("iPhone 16");
    }

    @Test
    @DisplayName("findById() - 查询不存在的订单返回 null")
    void findById_nonExistingOrder_shouldReturnNull() {
        // when
        Order order = orderService.findById(999L);

        // then
        assertThat(order).isNull();
    }

    @Test
    @DisplayName("create() - 创建新订单并返回带 ID 的订单")
    void create_shouldReturnOrderWithGeneratedId() {
        // given
        Long userId = 3L;
        String productName = "iPad";
        BigDecimal amount = new BigDecimal("3999.00");

        // when
        Order newOrder = orderService.create(userId, productName, amount);

        // then
        assertThat(newOrder).isNotNull();
        assertThat(newOrder.getId()).isNotNull();
        assertThat(newOrder.getId()).isGreaterThan(2L);
        assertThat(newOrder.getUserId()).isEqualTo(userId);
        assertThat(newOrder.getProductName()).isEqualTo(productName);
        assertThat(newOrder.getAmount()).isEqualByComparingTo(amount);
        assertThat(newOrder.getStatus()).isEqualTo("CREATED");
    }

    @Test
    @DisplayName("create() - 创建后可通过 findById 查询")
    void create_shouldBeQueryableAfterCreation() {
        // given
        Long userId = 4L;
        String productName = "Apple Watch";
        BigDecimal amount = new BigDecimal("2999.00");

        // when
        Order createdOrder = orderService.create(userId, productName, amount);
        Order foundOrder = orderService.findById(createdOrder.getId());

        // then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getId()).isEqualTo(createdOrder.getId());
        assertThat(foundOrder.getProductName()).isEqualTo(productName);
    }

    @Test
    @DisplayName("create() - 多次创建 ID 递增")
    void create_multipleTimes_shouldHaveIncrementingIds() {
        // when
        Order order1 = orderService.create(1L, "Product1", new BigDecimal("100"));
        Order order2 = orderService.create(2L, "Product2", new BigDecimal("200"));
        Order order3 = orderService.create(3L, "Product3", new BigDecimal("300"));

        // then
        assertThat(order1.getId()).isLessThan(order2.getId());
        assertThat(order2.getId()).isLessThan(order3.getId());
    }

    @Test
    @DisplayName("create() - 金额精度保留 2 位小数")
    void create_shouldPreserveDecimalPrecision() {
        // given
        BigDecimal amount = new BigDecimal("9999.99");

        // when
        Order order = orderService.create(1L, "Precision Test", amount);

        // then
        assertThat(order.getAmount().scale()).isEqualTo(2);
        assertThat(order.getAmount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("create() - 金额为 0 的订单")
    void create_shouldAllowZeroAmount() {
        // when
        Order order = orderService.create(1L, "Free Item", BigDecimal.ZERO);

        // then
        assertThat(order.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("create() - 大金额订单（百万级）")
    void create_shouldHandleLargeAmount() {
        // given
        BigDecimal largeAmount = new BigDecimal("1000000.00");

        // when
        Order order = orderService.create(1L, "Enterprise License", largeAmount);

        // then
        assertThat(order.getAmount()).isEqualByComparingTo(largeAmount);
    }

    @Test
    @DisplayName("create() - 商品名可以为空字符串")
    void create_shouldAllowEmptyProductName() {
        // when
        Order order = orderService.create(1L, "", new BigDecimal("100"));

        // then
        assertThat(order.getProductName()).isEmpty();
    }
}
