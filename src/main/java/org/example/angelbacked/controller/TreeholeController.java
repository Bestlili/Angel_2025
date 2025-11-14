package org.example.angelbacked.controller;

import org.example.angelbacked.entity.ChatSession;
import org.example.angelbacked.entity.ChatMessage;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.ChatService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/treehole")
public class TreeholeController {
    
    @Autowired
    private ChatService chatService;
    
    // 获取当前用户
    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }
    
    /**
     * 聊天接口
     * POST /api/treehole/chat
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody ChatRequest chatRequest, HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return Result.error("用户未登录");
            }
            
            // 获取AI回复
            String reply = chatService.chatWithAI(chatRequest.getMessage(), chatRequest.getSessionId(), user.getId());
            
            // 准备响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("reply", reply);
            responseData.put("sessionId", chatRequest.getSessionId() != null ? chatRequest.getSessionId() : UUID.randomUUID().toString());
            responseData.put("timestamp", LocalDateTime.now());
            
            return Result.success("操作成功").setData(responseData);
        } catch (Exception e) {
            return Result.error("聊天失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新会话
     * POST /api/treehole/session
     */
    @PostMapping("/session")
    public Result createSession(HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return Result.error("用户未登录");
            }
            
            ChatSession session = chatService.createSession(user.getId());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("sessionId", session.getId()); // 使用数据库生成的ID
            responseData.put("initialMessage", "你好！我是你的心理助手，有什么我可以帮你的吗？");
            
            return Result.success("创建成功").setData(responseData);
        } catch (Exception e) {
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取历史会话列表
     * GET /api/treehole/sessions
     */
    @GetMapping("/sessions")
    public Result getSessions(HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return Result.error("用户未登录");
            }
            
            List<ChatSession> sessions = chatService.getSessions(user.getId());
            return Result.success("获取成功").setData(sessions);
        } catch (Exception e) {
            return Result.error("获取会话列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取特定会话历史
     * GET /api/treehole/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public Result getSessionHistory(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return Result.error("用户未登录");
            }
            
            List<ChatMessage> messages = chatService.getSessionHistory(sessionId);
            return Result.success("获取成功").setData(messages);
        } catch (Exception e) {
            return Result.error("获取会话历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除会话
     * DELETE /api/treehole/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public Result deleteSession(@PathVariable String sessionId, HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = chatService.deleteSession(sessionId, user.getId());
            if (success) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                return Result.success("删除成功").setData(responseData);
            } else {
                return Result.error("会话不存在或无权限删除");
            }
        } catch (Exception e) {
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 快速回复接口
     * GET /api/treehole/quick-replies
     */
    @GetMapping("/quick-replies")
    public Result getQuickReplies() {
        try {
            List<String> quickReplies = chatService.getQuickReplies();
            return Result.success("获取成功").setData(quickReplies);
        } catch (Exception e) {
            return Result.error("获取快速回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 配置接口
     * GET /api/treehole/config
     */
    @GetMapping("/config")
    public Result getConfig() {
        try {
            Object config = chatService.getConfig();
            return Result.success("获取成功").setData(config);
        } catch (Exception e) {
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }
    
    // 内部类用于接收聊天请求数据
    public static class ChatRequest {
        private String message;
        private String sessionId;
        
        // Getters and setters
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}