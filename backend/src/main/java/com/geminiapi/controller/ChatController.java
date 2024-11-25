package com.geminiapi.controller;

import com.geminiapi.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/ask")
    public ResponseEntity<String> ask(@RequestBody String userInput) {
        System.out.println("userInput: " + userInput);
        return geminiService.getGeminiResponse(userInput);
    }
}
