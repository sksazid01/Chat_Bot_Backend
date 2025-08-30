package com.ChatBot.chatbot_service.controller;

import com.ChatBot.chatbot_service.dto.ChatRequest;
import com.ChatBot.chatbot_service.dto.ChatResponse;
import com.ChatBot.chatbot_service.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

        @Autowired
        private ChatService chatService;

        @GetMapping("health-check")
        public String healthCheck() {
                return "Hello, ChatBot Service is running!";
        }
        @PostMapping("chat")
        public String chat(@RequestBody ChatRequest chatRequest) {
            ChatResponse chatResponse = chatService.getResponse(chatRequest);
            return chatResponse.getResponse();
        }
}
