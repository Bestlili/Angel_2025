package org.example.angelbacked.service;

import org.example.angelbacked.entity.ChatSession;
import org.example.angelbacked.entity.ChatMessage;
import java.util.List;

public interface ChatService {
    // 聊天接口
    String chatWithAI(String message, String sessionId, Integer userId);
    
    // 会话管理接口
    ChatSession createSession(Integer userId);
    List<ChatSession> getSessions(Integer userId);
    List<ChatMessage> getSessionHistory(String sessionId);
    boolean deleteSession(String sessionId, Integer userId);
    
    // 快速回复接口
    List<String> getQuickReplies();
    
    // 配置接口
    Object getConfig();
}