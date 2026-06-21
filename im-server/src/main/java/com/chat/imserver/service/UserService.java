package com.chat.imserver.service;

import com.chat.imserver.entity.User;
import com.chat.imserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return 包含结果信息的 Map
     */
    public Map<String, Object> register(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 参数校验
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }

        // 检查用户名是否已被注册
        if (userRepository.existsByUsername(username)) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 生产环境应加密，教学项目先简化
        user.setNickname(username); // 默认昵称为用户名
        user.setAvatar("");         // 默认头像为空

        userRepository.save(user);

        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getId());
        return result;
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 包含结果信息的 Map
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 登录成功，返回用户基本信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("user", userInfo);
        return result;
    }
}