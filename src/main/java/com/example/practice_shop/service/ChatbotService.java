package com.example.practice_shop.service;

import com.example.practice_shop.dtos.chatbot.ChatbotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final RestTemplate restTemplate;

    public ChatbotDto.ChatMessageResponse processMessage(ChatbotDto.ChatMessageRequest request) {
        String userMessage = request.getMessage();
        String response = getRuleBasedResponse(userMessage);

        // 규칙 기반 응답이 없으면 RAG (FastAPI) 호출
        if (response.startsWith("죄송합니다")) {
            response = callRagServer(userMessage);
        }

        return ChatbotDto.ChatMessageResponse.builder()
                .response(response)
                .build();
    }

    private String callRagServer(String query) {
        try {
            // FastAPI Server URL
            String url = "http://localhost:8000/rag/search";

            // Request Payload
            Map<String, String> request = new HashMap<>();
            request.put("query", query);

            // Call API
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);
            Map<String, String> body = responseEntity.getBody();

            if (body != null && body.containsKey("answer")) {
                return body.get("answer");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 상담 서비스를 이용할 수 없습니다. (FastAPI 서버 연결 실패)";
        }
        return "죄송합니다. 답변을 찾을 수 없습니다.";
    }

    private String getRuleBasedResponse(String message) {
        if (message == null) {
            return "메시지를 입력해주세요.";
        }

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("안녕") || lowerMessage.contains("반가워")) {
            return "안녕하세요! 티켓 예매 사이트 챗봇입니다. 무엇을 도와드릴까요?";
        } else if (lowerMessage.contains("예매") || lowerMessage.contains("티켓") || lowerMessage.contains("구매")) {
            return "원하시는 공연이나 이벤트를 선택한 후 좌석을 지정하여 예매/결제를 진행해주세요.";
        } else if (lowerMessage.contains("취소") || lowerMessage.contains("환불")) {
            return "예매 취소 및 환불은 공연 관람일 1일 전까지 마이페이지에서 가능합니다. 당일 취소는 불가능합니다.";
        } else if (lowerMessage.contains("위치") || lowerMessage.contains("장소")) {
            return "공연장 위치는 공연 상세 페이지의 지도 안내를 참고해주세요.";
        } else if (lowerMessage.contains("시간") || lowerMessage.contains("문의")) {
            return "고객센터 운영 시간은 평일 오전 9시부터 오후 6시까지입니다.";
        } else {
            return "죄송합니다, 이해하지 못했습니다. (AI 검색 시도)";
        }
    }
}
