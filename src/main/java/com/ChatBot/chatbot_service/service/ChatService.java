package com.ChatBot.chatbot_service.service;

import com.ChatBot.chatbot_service.dto.ChatRequest;
import com.ChatBot.chatbot_service.dto.ChatResponse;
import com.ChatBot.chatbot_service.entity.ChatHistory;
import com.ChatBot.chatbot_service.entity.User;
import com.ChatBot.chatbot_service.repository.ChatHistoryRepository;
import com.ChatBot.chatbot_service.repository.UserRepository;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
        @Value("${gemini.api.key}")
        private String apiKey;
        private List<Content> conversationHistory = new ArrayList<>();

        @Autowired
        private ChatHistoryRepository chatHistoryRepository;

        @Autowired
        private UserRepository userRepository;

        public String processChat(ChatRequest chatRequest) {
                User user = userRepository.findByUsername(chatRequest.getUsername());
                if (user == null) {
                        throw new RuntimeException("User not found: " + chatRequest.getUsername());
                }

                // Build context from previous conversations
                String context = buildContextFromHistory(user);

                // Generate response using existing method with context
                String response = getResponseWithContext(chatRequest, context);

                // Save the conversation
                saveChatMessage(user, chatRequest.getMessage(), response);

                return response;
        }

        public ChatResponse getResponse(ChatRequest chatRequest) {
                Client client = Client.builder().apiKey(apiKey).build();

                Content content = Content.builder()
                                .role("user")
                                .parts(Part.builder()
                                                .text(chatRequest.getMessage()
                                                                + " Response should be concise and in less than 50 words.")
                                                .build())
                                .build();
                conversationHistory.add(content);

                GenerateContentResponse response = client.models.generateContent(
                                chatRequest.getModel(),
                                conversationHistory,
                                null);

                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setResponse(response.text());

                return chatResponse;
        }

        private String getResponseWithContext(ChatRequest chatRequest, String context) {
                Client client = Client.builder().apiKey(apiKey).build();

                // Prepare the content with context if available
                String fullMessage = chatRequest.getMessage();
                if (context != null && !context.trim().isEmpty()) {
                        fullMessage = context + "\n\nUser: " + chatRequest.getMessage();
                }

                Content content = Content.builder()
                                .role("user")
                                .parts(Part.builder()
                                                .text(fullMessage
                                                                + " Response should be concise and in less than 50 words.")
                                                .build())
                                .build();

                // Create a new conversation history for this request to include context
                List<Content> contextualHistory = new ArrayList<>();
                contextualHistory.add(content);

                GenerateContentResponse response = client.models.generateContent(
                                chatRequest.getModel(),
                                contextualHistory,
                                null);

                return response.text();
        }

        private String buildContextFromHistory(User user) {
                List<ChatHistory> recentChats = chatHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);

                if (recentChats.isEmpty()) {
                        return "This is the start of a new conversation.";
                }

                StringBuilder context = new StringBuilder("Previous conversation context:\n");

                // Reverse to show oldest first for better context flow
                for (int i = recentChats.size() - 1; i >= 0; i--) {
                        ChatHistory chat = recentChats.get(i);
                        context.append("User: ").append(chat.getUserMessage()).append("\n");
                        context.append("Assistant: ").append(chat.getBotResponse()).append("\n");
                }

                return context.toString();
        }

        private void saveChatMessage(User user, String userMessage, String botResponse) {
                long startTime = System.currentTimeMillis();

                ChatHistory chatHistory = new ChatHistory();
                chatHistory.setUser(user);
                chatHistory.setUserMessage(userMessage);
                chatHistory.setBotResponse(botResponse);
                chatHistory.setCreatedAt(LocalDateTime.now());

                long responseTime = System.currentTimeMillis() - startTime;
                chatHistory.setResponseTime(responseTime);

                chatHistoryRepository.save(chatHistory);
        }

}
