package com.xb.cloud.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 DTO —— 公共数据传输对象
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：订单 DTO 包含用户 ID 字段用于关联用户服务。
 * 前端传 userId，order-service 通过 OpenFeign 调用 user-service 获取用户详情。</p>
 */
public class OrderDTO {

    /** 订单 ID */
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 商品名称 */
    private String productName;
    /** 订单金额 */
    private BigDecimal amount;
    /** 订单状态：CREATED / PAID / SHIPPED / DONE */
    private String status;
    /** 创建时间 */
    private LocalDateTime createTime;

    public OrderDTO() {}

    public OrderDTO(Long id, Long userId, String productName, BigDecimal amount, String status) {
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