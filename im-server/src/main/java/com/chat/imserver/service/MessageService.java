package com.chat.imserver.service;

import com.chat.imserver.entity.Message;
import com.chat.imserver.entity.User;
import com.chat.imserver.repository.MessageRepository;
import com.chat.imserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // 临时存储离线消息（内存中），用于短轮询
    private final Map<String, List<Message>> pendingMessages = new HashMap<>();

    /**
     * 发送消息
     */
    public Map<String, Object> sendMessage(Long senderId, Long receiverId, String content) {
        Map<String, Object> result = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "消息内容不能为空");
            return result;
        }

        // 检查接收者是否存在
        if (!userRepository.existsById(receiverId)) {
            result.put("success", false);
            result.put("message", "接收用户不存在");
            return result;
        }

        // 保存消息到数据库
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        messageRepository.save(message);

        // 将消息放入待推送队列（供短轮询使用）
        String key = generateKey(senderId, receiverId);
        pendingMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(message);

        // 同时也放入接收者的全局待收队列
        String receiveKey = "receive_" + receiverId;
        pendingMessages.computeIfAbsent(receiveKey, k -> new ArrayList<>()).add(message);

        result.put("success", true);
        result.put("message", "发送成功");
        result.put("messageId", message.getId());
        result.put("timestamp", message.getCreatedAt().toString());
        return result;
    }

    /**
     * 获取聊天记录
     */
    public List<Map<String, Object>> getChatHistory(Long userId1, Long userId2) {
        List<Message> messages = messageRepository.findChatHistory(userId1, userId2);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Message msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", msg.getId());
            item.put("senderId", msg.getSenderId());
            item.put("receiverId", msg.getReceiverId());
            item.put("content", msg.getContent());
            item.put("timestamp", msg.getCreatedAt().toString());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取未读消息（短轮询接口）
     */
    public List<Map<String, Object>> pollMessages(Long userId) {
        String receiveKey = "receive_" + userId;
        List<Message> messages = pendingMessages.getOrDefault(receiveKey, new ArrayList<>());

        // 取出后清空（模拟"消费"掉这些消息）
        pendingMessages.remove(receiveKey);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", msg.getId());
            item.put("senderId", msg.getSenderId());
            item.put("receiverId", msg.getReceiverId());
            item.put("content", msg.getContent());
            item.put("timestamp", msg.getCreatedAt().toString());

            // 获取发送者信息
            User sender = userRepository.findById(msg.getSenderId()).orElse(null);
            if (sender != null) {
                item.put("senderName", sender.getNickname());
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 获取会话列表
     */
    public List<Map<String, Object>> getConversations(Long userId) {
        List<Message> recentMessages = messageRepository.findRecentConversations(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Message msg : recentMessages) {
            Map<String, Object> item = new HashMap<>();

            // 确定对方的ID
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            User otherUser = userRepository.findById(otherUserId).orElse(null);

            if (otherUser != null) {
                item.put("userId", otherUser.getId());
                item.put("username", otherUser.getUsername());
                item.put("nickname", otherUser.getNickname());
                item.put("lastMessage", msg.getContent());
                item.put("lastTime", msg.getCreatedAt().toString());
                result.add(item);
            }
        }
        return result;
    }

    private String generateKey(Long user1, Long user2) {
        // 生成唯一的会话键（小的ID在前）
        return Math.min(user1, user2) + "_" + Math.max(user1, user2);
    }
}