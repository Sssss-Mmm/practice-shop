package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.chatbot.ChatbotDto;
import com.example.practice_shop.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chatbot", description = "챗봇 API")
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(summary = "챗봇 메시지 전송", description = "사용자의 메시지를 받아 챗봇의 응답을 반환합니다.")
    @PostMapping("/chat")
    public ResponseEntity<ChatbotDto.ChatMessageResponse> chat(@RequestBody ChatbotDto.ChatMessageRequest request) {
        ChatbotDto.ChatMessageResponse response = chatbotService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
