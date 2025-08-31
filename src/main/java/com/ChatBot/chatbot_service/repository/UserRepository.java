package com.ChatBot.chatbot_service.repository;

import com.ChatBot.chatbot_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User , Long> {
   User findByUsername(String username);
}
