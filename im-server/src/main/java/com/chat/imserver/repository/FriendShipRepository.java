package com.chat.imserver.repository;

import com.chat.imserver.entity.FriendShip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendShipRepository extends JpaRepository<FriendShip, Long> {

    // 查询两个用户之间的好友关系
    Optional<FriendShip> findByUserIdAndFriendId(Long userId, Long friendId);

    // 查询用户的所有好友（状态为已确认）
    @Query("SELECT f FROM FriendShip f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = 1")
    List<FriendShip> findAllFriends(@Param("userId") Long userId);

    // 查询用户收到的待确认好友请求
    @Query("SELECT f FROM FriendShip f WHERE f.friendId = :userId AND f.status = 0")
    List<FriendShip> findPendingRequests(@Param("userId") Long userId);
}