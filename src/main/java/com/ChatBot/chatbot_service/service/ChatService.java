package com.ChatBot.chatbot_service.service;

import com.ChatBot.chatbot_service.dto.ChatRequest;
import com.ChatBot.chatbot_service.dto.ChatResponse;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ChatService {
    @Value("${gemini.api.key}")
    private String apiKey;

    public ChatResponse getResponse(ChatRequest chatRequest) {

        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response =
                client.models.generateContent(
                        chatRequest.getModel(),
                        chatRequest.getMessage()+" Response should be concise and in less than 50 words.",
                        null);

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setResponse(response.text());
        return chatResponse;
    }
}
