package com.ChatBot.chatbot_service.service;

import com.ChatBot.chatbot_service.dto.ChatRequest;
import com.ChatBot.chatbot_service.dto.ChatResponse;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    @Value("${gemini.api.key}")
    private String apiKey;
    private List<Content> conversationHistory = new ArrayList<>();

    public ChatResponse getResponse(ChatRequest chatRequest) {

        Client client = Client.builder().apiKey(apiKey).build();

        Content content = Content.builder()
                .role("user")
                .parts(Part.builder()
                        .text(chatRequest.getMessage()+" Response should be concise and in less than 50 words.")
                        .build())
                .build();
        conversationHistory.add(content);

        GenerateContentResponse response =
                client.models.generateContent(
                        chatRequest.getModel(),
                        conversationHistory,
                        null
                );

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponse(response.text());
        return chatResponse;
    }
}
