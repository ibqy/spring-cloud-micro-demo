package com.xb.cloud.common.dto;

/**
 * 用户 DTO —— 公共数据传输对象
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：微服务间通过 DTO 传输数据。DTO 放置在公共模块中，
 * 确保 all 微服务使用同一份类型定义，避免序列化/反序列化不一致。</p>
 */
public class UserDTO {

    /** 用户 ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 邮箱 */
    private String email;

    public UserDTO() {}

    public UserDTO(Long id, String username, String email) {
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