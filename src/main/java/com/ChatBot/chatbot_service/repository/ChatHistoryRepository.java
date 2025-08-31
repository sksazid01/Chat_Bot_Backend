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
    
    // Get recent detailed conversations (last 5 for detailed context)
    List<ChatHistory> findTop5ByUserOrderByCreatedAtDesc(User user);
    
    // Get older conversations that need summarization (excluding recent ones)
    List<ChatHistory> findByUserAndIsSummarizedFalseOrderByCreatedAtAsc(User user);
    
    // Get conversations older than a specific ID for summarization
    List<ChatHistory> findByUserAndIdLessThanOrderByCreatedAtAsc(User user, Long id);
    
    // Count total conversations for a user
    long countByUser(User user);
}


