package com.ChatBot.chatbot_service.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String username;
    private String model;
    private String message;
}

