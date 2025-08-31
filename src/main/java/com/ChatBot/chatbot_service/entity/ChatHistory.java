package com.ChatBot.chatbot_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_message", nullable = false, length = 1000)
    private String userMessage;

    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "response_time")
    private Long responseTime;

    @Column(name = "is_summarized")
    private Boolean isSummarized = false;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary="No summary";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    /**
     * Safe method to check if conversation is summarized, handling null values
     * @return true if summarized, false otherwise (including null cases)
     */
    public boolean isSummarizedSafely() {
        return Boolean.TRUE.equals(this.isSummarized);
    }
}
