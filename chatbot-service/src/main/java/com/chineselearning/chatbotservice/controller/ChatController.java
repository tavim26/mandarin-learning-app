package com.chineselearning.chatbotservice.controller;

import com.chineselearning.chatbotservice.dto.ChatMessageDto;
import com.chineselearning.chatbotservice.dto.ChatSessionDto;
import com.chineselearning.chatbotservice.dto.CreateSessionRequest;
import com.chineselearning.chatbotservice.dto.PagedResponse;
import com.chineselearning.chatbotservice.dto.SendMessageRequest;
import com.chineselearning.chatbotservice.dto.SendMessageResponse;
import com.chineselearning.chatbotservice.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService)
    {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionDto> createSession(
            @RequestHeader("X-User-Id") Long studentId,
            @Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createSession(studentId, request));
    }

    @GetMapping("/sessions")
    public List<ChatSessionDto> getSessions(@RequestHeader("X-User-Id") Long studentId) {
        return chatService.getSessions(studentId);
    }

    @GetMapping("/sessions/paged")
    public PagedResponse<ChatSessionDto> getSessionsPaged(
            @RequestHeader("X-User-Id") Long studentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {
        return chatService.getSessionsPaged(studentId, page, size);
    }

    @PatchMapping("/sessions/{sessionId}/end")
    public ChatSessionDto endSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId) {
        return chatService.endSession(sessionId, studentId);
    }

    @PatchMapping("/sessions/{sessionId}/rename")
    public ChatSessionDto renameSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestParam @NotBlank @Size(max = 255) String newTitle) {
        return chatService.renameSession(sessionId, studentId, newTitle);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId) {
        chatService.deleteSession(sessionId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<SendMessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(sessionId, studentId, request.content()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public List<ChatMessageDto> getMessages(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId) {
        return chatService.getMessages(sessionId, studentId);
    }

    @GetMapping("/sessions/{sessionId}/messages/paged")
    public PagedResponse<ChatMessageDto> getMessagesPaged(
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long studentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return chatService.getMessagesPaged(sessionId, studentId, page, size);
    }
}