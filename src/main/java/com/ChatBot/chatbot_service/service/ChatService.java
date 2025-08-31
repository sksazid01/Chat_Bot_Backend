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
        
        // Configuration for summarization
        private static final int RECENT_MESSAGES_LIMIT = 5; // Keep last 5 messages in detail
        private static final int SUMMARIZATION_THRESHOLD = 15; // Start summarizing after 15 messages

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
                // Check if we need to trigger summarization
                long totalConversations = chatHistoryRepository.countByUser(user);
                
                if (totalConversations > SUMMARIZATION_THRESHOLD) {
                        // Trigger summarization for older conversations
                        summarizeOlderConversations(user);
                }
                
                // Get recent detailed conversations (last 5)
                List<ChatHistory> recentChats = chatHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user);
                
                if (recentChats.isEmpty()) {
                        return "This is the start of a new conversation.";
                }
                
                StringBuilder context = new StringBuilder();
                
                // Add summarized context if available
                String summarizedContext = getSummarizedContext(user, recentChats);
                if (!summarizedContext.isEmpty()) {
                        context.append("Previous conversation summary:\n");
                        context.append(summarizedContext).append("\n\n");
                }
                
                // Add recent detailed conversations
                context.append("Recent conversation history:\n");
                
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
        
        /**
         * Summarize older conversations to save context space
         */
        private void summarizeOlderConversations(User user) {
                // Get recent conversations to find the cutoff point
                List<ChatHistory> recentChats = chatHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user);
                
                if (recentChats.isEmpty()) {
                        return;
                }
                
                // Get the oldest recent chat ID to use as cutoff
                Long cutoffId = recentChats.get(recentChats.size() - 1).getId();
                
                // Get older conversations that haven't been summarized
                List<ChatHistory> olderChats = chatHistoryRepository.findByUserAndIdLessThanOrderByCreatedAtAsc(user, cutoffId);
                
                if (olderChats.size() < 3) { // Only summarize if we have at least 3 older conversations
                        return;
                }
                
                // Group conversations into batches for summarization (every 5-10 conversations)
                int batchSize = 8;
                for (int i = 0; i < olderChats.size(); i += batchSize) {
                        int endIndex = Math.min(i + batchSize, olderChats.size());
                        List<ChatHistory> batch = olderChats.subList(i, endIndex);
                        
                        // Create summary for this batch
                        String summary = createConversationSummary(batch);
                        
                        // Update the last conversation in the batch with the summary
                        ChatHistory lastInBatch = batch.get(batch.size() - 1);
                        lastInBatch.setSummary(summary);
                        lastInBatch.setIsSummarized(true);
                        chatHistoryRepository.save(lastInBatch);
                        
                        // Mark other conversations in batch as summarized (to avoid reprocessing)
                        for (int j = 0; j < batch.size() - 1; j++) {
                                ChatHistory chat = batch.get(j);
                                chat.setIsSummarized(true);
                                chatHistoryRepository.save(chat);
                        }
                }
        }
        
        /**
         * Create a summary of a batch of conversations using AI
         */
        private String createConversationSummary(List<ChatHistory> conversations) {
                if (conversations.isEmpty()) {
                        return "";
                }
                
                StringBuilder conversationText = new StringBuilder();
                conversationText.append("Please summarize the following conversation in 2-3 sentences, focusing on key topics discussed and important context:\n\n");
                
                for (ChatHistory chat : conversations) {
                        conversationText.append("User: ").append(chat.getUserMessage()).append("\n");
                        conversationText.append("Assistant: ").append(chat.getBotResponse()).append("\n");
                }
                
                try {
                        Client client = Client.builder().apiKey(apiKey).build();
                        
                        Content content = Content.builder()
                                .role("user")
                                .parts(Part.builder()
                                        .text(conversationText.toString())
                                        .build())
                                .build();
                        
                        List<Content> summaryHistory = new ArrayList<>();
                        summaryHistory.add(content);
                        
                        GenerateContentResponse response = client.models.generateContent(
                                "gemini-1.5-flash",
                                summaryHistory,
                                null);
                        
                        return response.text();
                        
                } catch (Exception e) {
                        // Fallback to simple summary if AI summarization fails
                        return "Conversation covered topics: " + extractKeyTopics(conversations);
                }
        }
        
        /**
         * Get summarized context from previous conversation summaries
         */
        private String getSummarizedContext(User user, List<ChatHistory> recentChats) {
                if (recentChats.isEmpty()) {
                        return "";
                }
                
                // Get the oldest recent chat ID to find summaries before it
                Long oldestRecentId = recentChats.get(recentChats.size() - 1).getId();
                
                // Find conversations with summaries that are older than recent ones
                List<ChatHistory> conversationsWithSummaries = chatHistoryRepository.findByUserAndIdLessThanOrderByCreatedAtAsc(user, oldestRecentId);
                
                StringBuilder summarizedContext = new StringBuilder();
                
                for (ChatHistory chat : conversationsWithSummaries) {
                        // Use safe method to handle null cases for existing records
                        if (chat.isSummarizedSafely() && chat.getSummary() != null && !chat.getSummary().trim().isEmpty()) {
                                summarizedContext.append(chat.getSummary()).append(" ");
                        }
                }
                
                return summarizedContext.toString().trim();
        }
        
        /**
         * Fallback method to extract key topics when AI summarization fails
         */
        private String extractKeyTopics(List<ChatHistory> conversations) {
                // Simple keyword extraction as fallback
                StringBuilder topics = new StringBuilder();
                int count = 0;
                
                for (ChatHistory chat : conversations) {
                        String message = chat.getUserMessage().toLowerCase();
                        if (message.contains("question") || message.contains("help") || message.contains("how")) {
                                topics.append("questions, ");
                                count++;
                        }
                        if (count >= 3) break; // Limit fallback summary
                }
                
                return topics.length() > 0 ? topics.substring(0, topics.length() - 2) : "general discussion";
        }

}
