package com.chat.imserver.repository;

import com.chat.imserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户（用于登录校验）
    Optional<User> findByUsername(String username);

    // 判断用户名是否已存在（用于注册校验）
    boolean existsByUsername(String username);
}