package com.xb.cloud.user.service;

import com.xb.cloud.user.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务 —— 内存实现
 *
 * <p>作者：xb | 日期：2026-09-12</p>
 *
 * <p><b>教学知识点</b>：Service 层封装业务逻辑。启动时 {@code @PostConstruct}
 * 初始化两条演示数据，方便快速测试。</p>
 */
@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostConstruct
    public void init() {
        save("zhangsan", "zhangsan@example.com");
        save("lisi", "lisi@example.com");
    }

    /** 根据 ID 查询用户 */
    public User findById(Long id) {
        return users.get(id);
    }

    /** 创建用户 */
    public User create(String username, String email) {
        return save(username, email);
    }

    private User save(String username, String email) {
        User u = new User(idGen.getAndIncrement(), username, email);
        users.put(u.getId(), u);
        return u;
    }
}