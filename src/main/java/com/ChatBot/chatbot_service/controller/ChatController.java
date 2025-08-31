package com.ChatBot.chatbot_service.controller;

import com.ChatBot.chatbot_service.dto.ChatRequest;
import com.ChatBot.chatbot_service.repository.UserRepository;
import com.ChatBot.chatbot_service.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {
        
        @Autowired
        private ChatService chatService;

        @GetMapping("health-check")
        public String healthCheck() {
                return "Hello, ChatBot Service is running!";
        }

        @Autowired
        UserRepository userRepository;

        
        @PostMapping("chat")
        public ResponseEntity<Map<String, String>> sendMessage(@RequestBody ChatRequest chatRequest) {
            try {
                String username = chatRequest.getUsername();
                String message = chatRequest.getMessage();
                
                if (username == null || message == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Username and message are required"));
                }
                
                String response = chatService.processChat(chatRequest);
                
                return ResponseEntity.ok(Map.of(
                        "response", response,
                        "status", "success"
                ));
                
            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", e.getMessage()));
            }
        }
}
