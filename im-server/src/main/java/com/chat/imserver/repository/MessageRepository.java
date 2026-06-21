package com.chat.imserver.repository;

import com.chat.imserver.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 查询两个用户之间的聊天记录（按时间升序）
    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1 AND m.receiverId = :user2) OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("user1") Long user1, @Param("user2") Long user2);

    // 查询用户的所有未读消息（按时间升序）
    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.read = false ORDER BY m.createdAt ASC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);

    // 获取用户最近的会话列表（每个会话的最新一条消息）
    @Query(value = "SELECT m.* FROM messages m WHERE m.id IN (SELECT MAX(m2.id) FROM messages m2 WHERE m2.sender_id = :userId OR m2.receiver_id = :userId GROUP BY CASE WHEN m2.sender_id = :userId THEN m2.receiver_id ELSE m2.sender_id END) ORDER BY m.created_at DESC", nativeQuery = true)
    List<Message> findRecentConversations(@Param("userId") Long userId);
}