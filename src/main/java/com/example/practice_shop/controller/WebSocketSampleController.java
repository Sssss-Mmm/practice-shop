// package com.example.practice_shop.controller;
//
// import lombok.RequiredArgsConstructor;
// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Controller;
//
// @Controller
// @RequiredArgsConstructor
// public class WebSocketSampleController {
//
//     private final SimpMessagingTemplate messagingTemplate;
//
//     /**
//      * 클라이언트가 /app/ping 으로 보내면 /topic/ping 으로 브로드캐스트
//      */
//     @MessageMapping("/ping")
//     public void ping(@Payload String message) {
//         messagingTemplate.convertAndSend("/topic/ping", message == null || message.isBlank() ? "pong" : message);
//     }
// }
