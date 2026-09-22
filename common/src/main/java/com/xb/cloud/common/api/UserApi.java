package com.xb.cloud.common.api;

import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务 API 接口 —— 公共定义
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：将 API 接口定义放在 common 模块中，
 * user-service 实现这些接口，order-service 通过 OpenFeign 继承这些接口。
 * 这种方式称为"契约优先"，保证客户端和服务端的接口始终一致。</p>
 *
 * @author ibqy
 */
public interface UserApi {

    /** 根据 ID 查询用户 */
    @GetMapping("/api/users/{id}")
    Result<UserDTO> getUserById(@PathVariable Long id);

    /** 创建用户 */
    @PostMapping("/api/users")
    Result<UserDTO> createUser(@RequestBody UserDTO userDTO);
}