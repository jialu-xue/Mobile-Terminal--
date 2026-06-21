package com.chat.imserver.controller;

import com.chat.imserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 注册接口
     * POST /api/auth/register
     * 请求体：{"username": "xxx", "password": "xxx"}
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return userService.register(username, password);
    }

    /**
     * 登录接口
     * POST /api/auth/login
     * 请求体：{"username": "xxx", "password": "xxx"}
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return userService.login(username, password);
    }
}