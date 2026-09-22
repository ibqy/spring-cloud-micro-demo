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
public class UserService {

    // 用 ConcurrentHashMap 模拟数据库，保证并发安全
    private final Map<Long, User> users = new ConcurrentHashMap<>();
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
        save("zhangsan", "zhangsan@example.com");
        save("lisi", "lisi@example.com");
    }

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户对象，不存在时返回 null
     */
    public User findById(Long id) {
        return users.get(id);
    }

    /**
     * 创建新用户
     *
     * <p>自动生成 ID，由 {@code AtomicLong} 保证唯一递增。</p>
     *
     * @param username 用户名
     * @param email    邮箱
     * @return 创建成功的用户对象
     */
    public User create(String username, String email) {
        return save(username, email);
    }

    private User save(String username, String email) {
        User u = new User(idGen.getAndIncrement(), username, email);
        users.put(u.getId(), u);
        return u;
    }
}