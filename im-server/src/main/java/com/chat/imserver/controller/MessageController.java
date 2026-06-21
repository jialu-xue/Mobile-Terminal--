package com.chat.imserver.controller;

import com.chat.imserver.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     * POST /api/message/send
     * {"senderId": 1, "receiverId": 2, "content": "你好"}
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> request) {
        Long senderId = Long.valueOf(request.get("senderId").toString());
        Long receiverId = Long.valueOf(request.get("receiverId").toString());
        String content = (String) request.get("content");
        return messageService.sendMessage(senderId, receiverId, content);
    }

    /**
     * 获取聊天记录
     * GET /api/message/history?userId1=1&userId2=2
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getChatHistory(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        return messageService.getChatHistory(userId1, userId2);
    }

    /**
     * 轮询新消息（短轮询）
     * GET /api/message/poll?userId=1
     */
    @GetMapping("/poll")
    public List<Map<String, Object>> pollMessages(@RequestParam Long userId) {
        return messageService.pollMessages(userId);
    }

    /**
     * 获取会话列表
     * GET /api/message/conversations?userId=1
     */
    @GetMapping("/conversations")
    public List<Map<String, Object>> getConversations(@RequestParam Long userId) {
        return messageService.getConversations(userId);
    }
}