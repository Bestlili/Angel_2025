package org.example.angelbacked.controller;

import org.example.angelbacked.util.Result;
import org.example.angelbacked.util.VolcengineAIUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class AIModelTestController {

    @Autowired
    private VolcengineAIUtil volcengineAIUtil;

    /**
     * 测试火山引擎AI模型
     * POST /api/test/volcengine-ai
     */
    @PostMapping("/volcengine-ai")
    public Result testVolcengineAI(@RequestBody AIRequest aiRequest) {
        try {
            // 构建上下文
            List<Map<String, String>> context = new ArrayList<>();
            if (aiRequest.getContext() != null) {
                context = aiRequest.getContext();
            }

            // 调用火山引擎AI
            String response = volcengineAIUtil.chatWithAI(aiRequest.getMessage(), context);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("response", response);
            responseData.put("model", "Doubao-Seed-1.6");

            return Result.success("AI模型调用成功").setData(responseData);
        } catch (Exception e) {
            return Result.error("AI模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 测试火山引擎AI模型（流式）
     * POST /api/test/volcengine-ai/stream
     */
    @PostMapping("/volcengine-ai/stream")
    public Result testVolcengineAIStream(@RequestBody AIRequest aiRequest) {
        try {
            // 构建上下文
            List<Map<String, String>> context = new ArrayList<>();
            if (aiRequest.getContext() != null) {
                context = aiRequest.getContext();
            }

            // 调用火山引擎AI流式接口
            String response = volcengineAIUtil.streamChatWithAI(aiRequest.getMessage(), context);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("response", response);
            responseData.put("model", "Doubao-Seed-1.6 (stream)");

            return Result.success("AI模型流式调用成功").setData(responseData);
        } catch (Exception e) {
            return Result.error("AI模型流式调用失败: " + e.getMessage());
        }
    }

    // 内部类用于接收AI请求数据
    public static class AIRequest {
        private String message;
        private List<Map<String, String>> context;

        // Getters and setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Map<String, String>> getContext() {
            return context;
        }

        public void setContext(List<Map<String, String>> context) {
            this.context = context;
        }
    }
}