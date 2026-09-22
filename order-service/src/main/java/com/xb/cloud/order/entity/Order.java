package com.xb.cloud.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 —— 内存存储模型
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：订单实体包含 {@code userId} 关联用户，
 * 查询时通过 OpenFeign 调用 user-service 获取用户信息，演示微服务间的数据协作。</p>
 *
 * @author ibqy
 */
public class Order {

    private Long id;
    private Long userId;
    private String productName;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createTime;

    public Order() {}

    public Order(Long id, Long userId, String productName, BigDecimal amount, String status) {
        this.id = id;
        this.userId = userId;
        this.productName = productName;
        this.amount = amount;
        this.status = status;
        this.createTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}