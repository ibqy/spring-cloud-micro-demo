package com.xb.cloud.user.service;

import com.xb.cloud.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserService 单元测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p>验证用户服务的核心业务逻辑：初始化数据、查询、创建。</p>
 */
@DisplayName("UserService 单元测试")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.init();
    }

    @Test
    @DisplayName("init() - 初始化后应有 2 条演示数据")
    void init_shouldCreateTwoDemoUsers() {
        // when
        User user1 = userService.findById(1L);
        User user2 = userService.findById(2L);

        // then
        assertThat(user1).isNotNull();
        assertThat(user1.getUsername()).isEqualTo("zhangsan");
        assertThat(user1.getEmail()).isEqualTo("zhangsan@example.com");

        assertThat(user2).isNotNull();
        assertThat(user2.getUsername()).isEqualTo("lisi");
        assertThat(user2.getEmail()).isEqualTo("lisi@example.com");
    }

    @Test
    @DisplayName("findById() - 查询存在的用户")
    void findById_existingUser_shouldReturnUser() {
        // when
        User user = userService.findById(1L);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    @DisplayName("findById() - 查询不存在的用户返回 null")
    void findById_nonExistingUser_shouldReturnNull() {
        // when
        User user = userService.findById(999L);

        // then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("create() - 创建新用户并返回带 ID 的用户")
    void create_shouldReturnUserWithGeneratedId() {
        // given
        String username = "wangwu";
        String email = "wangwu@example.com";

        // when
        User newUser = userService.create(username, email);

        // then
        assertThat(newUser).isNotNull();
        assertThat(newUser.getId()).isNotNull();
        assertThat(newUser.getId()).isGreaterThan(2L);
        assertThat(newUser.getUsername()).isEqualTo(username);
        assertThat(newUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("create() - 创建后可通过 findById 查询")
    void create_shouldBeQueryableAfterCreation() {
        // given
        String username = "zhaoliu";
        String email = "zhaoliu@example.com";

        // when
        User createdUser = userService.create(username, email);
        User foundUser = userService.findById(createdUser.getId());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("create() - 多次创建 ID 递增")
    void create_multipleTimes_shouldHaveIncrementingIds() {
        // when
        User user1 = userService.create("user1", "user1@example.com");
        User user2 = userService.create("user2", "user2@example.com");
        User user3 = userService.create("user3", "user3@example.com");

        // then
        assertThat(user1.getId()).isLessThan(user2.getId());
        assertThat(user2.getId()).isLessThan(user3.getId());
    }

    @Test
    @DisplayName("create() - 用户名可以为空字符串")
    void create_shouldAllowEmptyUsername() {
        // when
        User user = userService.create("", "empty@example.com");

        // then
        assertThat(user.getUsername()).isEmpty();
    }

    @Test
    @DisplayName("create() - 邮箱可以为空字符串")
    void create_shouldAllowEmptyEmail() {
        // when
        User user = userService.create("testuser", "");

        // then
        assertThat(user.getEmail()).isEmpty();
    }
}
