package com.ChatBot.chatbot_service.dto;

import lombok.Data;

@Data
public class ChatRequest {
    public String model;
    public String message;
}

