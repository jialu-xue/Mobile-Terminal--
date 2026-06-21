package com.chat.imserver.controller;

import com.chat.imserver.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 搜索用户
     * GET /api/friend/search?keyword=xxx&userId=1
     */
    @GetMapping("/search")
    public List<Map<String, Object>> searchUsers(
            @RequestParam String keyword,
            @RequestParam Long userId) {
        return friendService.searchUsers(keyword, userId);
    }

    /**
     * 发送好友请求
     * POST /api/friend/request
     * {"userId": 1, "friendId": 2}
     */
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long friendId = request.get("friendId");
        return friendService.sendRequest(userId, friendId);
    }

    /**
     * 接受好友请求
     * POST /api/friend/accept
     * {"requestId": 1}
     */
    @PostMapping("/accept")
    public Map<String, Object> acceptRequest(@RequestBody Map<String, Long> request) {
        Long requestId = request.get("requestId");
        return friendService.acceptRequest(requestId);
    }

    /**
     * 获取好友列表
     * GET /api/friend/list?userId=1
     */
    @GetMapping("/list")
    public List<Map<String, Object>> getFriendList(@RequestParam Long userId) {
        return friendService.getFriendList(userId);
    }

    /**
     * 获取待处理的好友请求
     * GET /api/friend/pending?userId=1
     */
    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingRequests(@RequestParam Long userId) {
        return friendService.getPendingRequests(userId);
    }
}