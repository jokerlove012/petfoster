package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 助手接口
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    
    private final AiService aiService;
    
    /**
     * 发送消息给AI
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        
        // 获取历史消息（可选）
        List<Map<String, String>> history = null;
        if (request.containsKey("history")) {
            history = (List<Map<String, String>>) request.get("history");
        }
        
        Map<String, Object> result = aiService.chat(message.trim(), history);
        return Result.success(result);
    }
}
