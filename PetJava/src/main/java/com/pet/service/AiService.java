package com.pet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek AI 服务
 */
@Slf4j
@Service
public class AiService {
    
    // DeepSeek API 配置 - 从配置文件读取
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 系统提示词 - 定义AI助手的角色
    private static final String SYSTEM_PROMPT = 
        "你是一个专业的宠物寄养平台AI助手，名叫「宠宠助手」。你的职责是：\n" +
        "1. 帮助用户了解宠物寄养相关知识\n" +
        "2. 推荐合适的寄养机构\n" +
        "3. 解答宠物护理、健康、行为等问题\n" +
        "4. 提供寄养前的准备建议\n" +
        "5. 帮助用户比较不同机构的服务和价格\n\n" +
        "回复要求：\n" +
        "- 回复要友好、专业、有帮助\n" +
        "- 适当使用emoji让回复更生动\n" +
        "- 如果涉及具体机构推荐，说明这是示例，建议用户在平台上搜索\n" +
        "- 回复控制在300字以内，重点突出\n" +
        "- 使用中文回复";

    /**
     * 发送消息给 DeepSeek AI
     */
    public Map<String, Object> chat(String userMessage, List<Map<String, String>> history) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查 API Key
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置，使用模拟回复");
            result.put("success", true);
            result.put("content", generateMockReply(userMessage));
            result.put("mock", true);
            return result;
        }
        
        try {
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);
            
            // 添加历史消息（最近10条）
            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - 10);
                for (int i = start; i < history.size(); i++) {
                    messages.add(history.get(i));
                }
            }
            
            // 添加当前用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            log.info("发送请求到 DeepSeek API: {}", userMessage.substring(0, Math.min(50, userMessage.length())));
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                List<Map> choices = (List<Map>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map choice = choices.get(0);
                    Map message = (Map) choice.get("message");
                    String content = (String) message.get("content");
                    
                    result.put("success", true);
                    result.put("content", content);
                    result.put("mock", false);
                    
                    // 返回 token 使用情况
                    Map usage = (Map) body.get("usage");
                    if (usage != null) {
                        result.put("usage", usage);
                    }
                    
                    log.info("DeepSeek API 响应成功");
                    return result;
                }
            }
            
            throw new RuntimeException("API 响应格式异常");
            
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败: {}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            // 失败时返回模拟回复
            result.put("content", generateMockReply(userMessage));
            result.put("mock", true);
            return result;
        }
    }
    
    /**
     * 生成模拟回复（API不可用时的备用方案）
     */
    private String generateMockReply(String question) {
        String q = question.toLowerCase();
        
        if (q.contains("机构") && (q.contains("推荐") || q.contains("附近"))) {
            return "🏠 根据您的需求，我建议您在平台首页搜索附近的寄养机构。\n\n" +
                   "选择机构时可以关注：\n" +
                   "• ⭐ 用户评分和评价数量\n" +
                   "• 📋 提供的服务套餐\n" +
                   "• 💰 价格是否透明\n" +
                   "• 📍 距离您的位置\n\n" +
                   "需要我帮您介绍如何筛选机构吗？";
        }
        
        if (q.contains("猫") && q.contains("注意")) {
            return "🐱 猫咪寄养注意事项：\n\n" +
                   "**寄养前**\n" +
                   "• 确保疫苗接种完整\n" +
                   "• 准备猫咪常用的猫粮\n" +
                   "• 带上熟悉的玩具或毯子\n\n" +
                   "**选择机构**\n" +
                   "• 优先选择猫咪专属寄养\n" +
                   "• 确认有独立猫房\n" +
                   "• 了解每日护理安排\n\n" +
                   "还有其他问题吗？";
        }
        
        if (q.contains("价格") || q.contains("多少钱")) {
            return "💰 寄养价格参考：\n\n" +
                   "• 基础寄养：¥58-88/天\n" +
                   "• 标准寄养：¥88-128/天\n" +
                   "• 豪华寄养：¥158-288/天\n\n" +
                   "价格因宠物体型、服务内容、机构档次而异。\n" +
                   "建议在平台上比较多家机构的报价~";
        }
        
        return "🐾 感谢您的提问！\n\n" +
               "作为宠物寄养助手，我可以帮您：\n" +
               "• 🔍 了解寄养机构信息\n" +
               "• 💡 解答宠物护理问题\n" +
               "• 📋 提供寄养注意事项\n\n" +
               "请告诉我您的具体需求~";
    }
}
