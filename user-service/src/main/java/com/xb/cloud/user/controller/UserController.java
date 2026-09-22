package com.xb.cloud.user.controller;

import com.xb.cloud.common.api.UserApi;
import com.xb.cloud.common.dto.Result;
import com.xb.cloud.common.dto.UserDTO;
import com.xb.cloud.user.entity.User;
import com.xb.cloud.user.service.UserService;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务 Controller —— 实现 UserApi 接口
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：Controller 实现 common 模块中定义的 {@link UserApi} 接口，
 * 这样 OpenFeign 客户端可以直接通过接口继承实现类型安全的远程调用。</p>
 *
 * @author ibqy
 */
@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Result<UserDTO> getUserById(Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return Result.fail("用户不存在，id=" + id);
        }
        return Result.ok(new UserDTO(user.getId(), user.getUsername(), user.getEmail()));
    }

    @Override
    public Result<UserDTO> createUser(UserDTO userDTO) {
        User user = userService.create(userDTO.getUsername(), userDTO.getEmail());
        return Result.ok(new UserDTO(user.getId(), user.getUsername(), user.getEmail()));
    }
}