package com.chat.imserver.service;

import com.chat.imserver.entity.FriendShip;
import com.chat.imserver.entity.User;
import com.chat.imserver.repository.FriendShipRepository;
import com.chat.imserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FriendService {

    @Autowired
    private FriendShipRepository friendShipRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 搜索用户（根据用户名模糊搜索）
     */
    public List<Map<String, Object>> searchUsers(String keyword, Long currentUserId) {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            // 排除自己
            if (user.getId().equals(currentUserId)) continue;

            // 模糊匹配用户名或昵称
            if (user.getUsername().contains(keyword) || user.getNickname().contains(keyword)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("username", user.getUsername());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());

                // 检查是否已经是好友
                boolean isFriend = friendShipRepository.findByUserIdAndFriendId(currentUserId, user.getId())
                        .map(f -> f.getStatus() == 1)
                        .orElse(false);
                if (!isFriend) {
                    isFriend = friendShipRepository.findByUserIdAndFriendId(user.getId(), currentUserId)
                            .map(f -> f.getStatus() == 1)
                            .orElse(false);
                }
                item.put("isFriend", isFriend);

                result.add(item);
            }
        }
        return result;
    }

    /**
     * 发送好友请求
     */
    public Map<String, Object> sendRequest(Long userId, Long friendId) {
        Map<String, Object> result = new HashMap<>();

        if (userId.equals(friendId)) {
            result.put("success", false);
            result.put("message", "不能添加自己为好友");
            return result;
        }

        // 检查是否已存在关系
        Optional<FriendShip> existing = friendShipRepository.findByUserIdAndFriendId(userId, friendId);
        if (existing.isPresent()) {
            result.put("success", false);
            result.put("message", "已经发送过好友请求或已是好友");
            return result;
        }

        // 检查反向关系
        existing = friendShipRepository.findByUserIdAndFriendId(friendId, userId);
        if (existing.isPresent()) {
            FriendShip fs = existing.get();
            if (fs.getStatus() == 1) {
                result.put("success", false);
                result.put("message", "已经是好友");
                return result;
            } else {
                // 对方已经向你发送请求，直接接受
                fs.setStatus(1);
                friendShipRepository.save(fs);
                result.put("success", true);
                result.put("message", "对方已向你发送请求，已自动成为好友");
                return result;
            }
        }

        // 创建新的好友请求
        FriendShip friendship = new FriendShip();
        friendship.setUserId(userId);
        friendship.setFriendId(friendId);
        friendship.setStatus(0); // 待确认
        friendShipRepository.save(friendship);

        result.put("success", true);
        result.put("message", "好友请求已发送");
        return result;
    }

    /**
     * 接受好友请求
     */
    public Map<String, Object> acceptRequest(Long requestId) {
        Map<String, Object> result = new HashMap<>();

        FriendShip friendship = friendShipRepository.findById(requestId).orElse(null);
        if (friendship == null) {
            result.put("success", false);
            result.put("message", "请求不存在");
            return result;
        }

        friendship.setStatus(1);
        friendShipRepository.save(friendship);

        result.put("success", true);
        result.put("message", "已接受好友请求");
        return result;
    }

    /**
     * 获取好友列表
     */
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<FriendShip> friendships = friendShipRepository.findAllFriends(userId);
        List<Map<String, Object>> friends = new ArrayList<>();

        for (FriendShip fs : friendships) {
            Long friendId = fs.getUserId().equals(userId) ? fs.getFriendId() : fs.getUserId();
            User friend = userRepository.findById(friendId).orElse(null);
            if (friend != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", friend.getId());
                item.put("username", friend.getUsername());
                item.put("nickname", friend.getNickname());
                item.put("avatar", friend.getAvatar());
                friends.add(item);
            }
        }
        return friends;
    }

    /**
     * 获取待处理的好友请求
     */
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<FriendShip> requests = friendShipRepository.findPendingRequests(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (FriendShip req : requests) {
            User requester = userRepository.findById(req.getUserId()).orElse(null);
            if (requester != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("requestId", req.getId());
                item.put("userId", requester.getId());
                item.put("username", requester.getUsername());
                item.put("nickname", requester.getNickname());
                result.add(item);
            }
        }
        return result;
    }
}