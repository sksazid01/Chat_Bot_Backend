package com.ChatBot.chatbot_service.repository;

import com.ChatBot.chatbot_service.entity.ChatHistory;
import com.ChatBot.chatbot_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserOrderByCreatedAtAsc(User user);
    List<ChatHistory> findByUserOrderByCreatedAtDesc(User user);
    
    // Get recent chat history for context (last N conversations)
    List<ChatHistory> findTop10ByUserOrderByCreatedAtDesc(User user);
}


