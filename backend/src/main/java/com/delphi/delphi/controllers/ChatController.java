package com.delphi.delphi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.services.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/messages")
    public ResponseEntity<?> createMessage(@RequestBody ChatMessage message) {
        try {
            ChatMessage createdMessage = chatService.createMessage(message);
            return ResponseEntity.ok(createdMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating message: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{chatHistoryId}")
    public ResponseEntity<?> getMessage(@PathVariable Long chatHistoryId) {
        try {
            ChatMessage message = chatService.getMessageById(chatHistoryId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting message: " + e.getMessage());
        }
    }
    
}
