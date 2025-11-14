package org.example.angelbacked.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.angelbacked.util.VolcengineAIUtil;

@Component
public class AIUtil {
    
    @Value("${ai.api.key:cddee0f6-1bb3-40b0-a82b-68faf21cbd2a}")
    private String apiKey;
    
    @Value("${ai.api.endpoint:https://api.openai.com/v1/chat/completions}")
    private String apiEndpoint;
    
    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${ai.temperature:0.7}")
    private Double temperature;
    
    @Value("${ai.provider:openai}")
    private String provider;
    
    private final VolcengineAIUtil volcengineAIUtil;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AIUtil(VolcengineAIUtil volcengineAIUtil) {
        this.volcengineAIUtil = volcengineAIUtil;
    }
    
    /**
     * 发送消息到AI并获取回复
     * 
     * @param message 用户消息
     * @param sessionId 会话ID，用于维持上下文
     * @param context 历史对话上下文
     * @return AI回复内容
     */
    public String chatWithAI(String message, String sessionId, List<Map<String, String>> context) {
        // 根据provider选择不同的AI服务
        if ("volcengine".equalsIgnoreCase(provider)) {
            return volcengineAIUtil.chatWithAI(message, context);
        }
        
        // 默认使用OpenAI服务
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 创建POST请求
            HttpPost httpPost = new HttpPost(apiEndpoint);
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            httpPost.setHeader("Content-Type", "application/json");
            
            // 构建请求体
            JsonNode requestBody = buildRequestBody(message, context);
            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
            httpPost.setEntity(entity);
            
            // 发送请求
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            
            if (responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);
                JsonNode responseJson = objectMapper.readTree(responseString);
                
                // 解析AI回复，根据具体API格式调整
                return parseAIResponse(responseJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 出错时返回默认消息
            return "抱歉，我现在无法回答，请稍后再试。";
        }
        
        return "抱歉，我没有理解你的意思。";
    }
    
    /**
     * 构建请求体
     */
    private JsonNode buildRequestBody(String message, List<Map<String, String>> context) {
        try {
            // 创建系统角色消息（设定AI的行为）
            Map<String, String> systemMessage = Map.of(
                "role", "system",
                "content", "你是一位专业的心理咨询师，你的名字叫心灵树洞。你的职责是倾听用户的困扰，给予温暖的回应和适当的建议。请用温和、理解和支持的语气与用户交流，避免评判性的语言。如果用户有严重的心理问题，请建议他们寻求专业帮助。"
            );
            
            // 创建用户当前消息
            Map<String, String> userMessage = Map.of(
                "role", "user",
                "content", message
            );
            
            // 组合所有消息
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(systemMessage);
            
            // 添加历史上下文
            if (context != null && !context.isEmpty()) {
                messages.addAll(context);
            }
            
            // 添加当前用户消息
            messages.add(userMessage);
            
            // 构建请求体
            return objectMapper.createObjectNode()
                .putPOJO("messages", messages)
                .put("model", model)
                .put("temperature", temperature);
        } catch (Exception e) {
            e.printStackTrace();
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * 解析AI响应
     */
    private String parseAIResponse(JsonNode responseJson) {
        try {
            // OpenAI/ChatGPT格式: {"choices": [{"message": {"content": "回复内容"}}]}
            if (responseJson.has("choices")) {
                JsonNode choices = responseJson.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    if (firstChoice.has("message")) {
                        JsonNode message = firstChoice.get("message");
                        if (message.has("content")) {
                            return message.get("content").asText();
                        }
                    }
                }
            }
            
            // 错误响应处理
            if (responseJson.has("error")) {
                JsonNode error = responseJson.get("error");
                if (error.has("message")) {
                    System.err.println("AI API Error: " + error.get("message").asText());
                }
                return "抱歉，我在处理您的请求时遇到了一些问题，请稍后再试。";
            }
            
            // 其他可能的格式
            if (responseJson.has("result")) {
                return responseJson.get("result").asText();
            }
            
            if (responseJson.has("response")) {
                return responseJson.get("response").asText();
            }
            
            if (responseJson.has("answer")) {
                return responseJson.get("answer").asText();
            }
            
            // 如果没有匹配的格式，返回完整响应的字符串表示
            return responseJson.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我在处理回复时遇到了一些问题。";
        }
    }
}