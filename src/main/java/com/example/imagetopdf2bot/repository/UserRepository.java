package com.example.imagetopdf2bot.repository;

import com.example.imagetopdf2bot.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findUsersByChatId(Long chatId);
}
