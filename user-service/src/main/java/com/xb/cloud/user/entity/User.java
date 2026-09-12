package com.xb.cloud.user.entity;

/**
 * 用户实体 —— 内存存储模型
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：为简化教学，本示例使用 {@code ConcurrentHashMap} 做内存存储，
 * 演示微服务架构的核心交互，不引入数据库依赖。</p>
 */
public class User {

    private Long id;
    private String username;
    private String email;

    public User() {}

    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}