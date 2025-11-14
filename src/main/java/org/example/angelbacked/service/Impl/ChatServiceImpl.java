package org.example.angelbacked.service.Impl;

import org.example.angelbacked.entity.ChatSession;
import org.example.angelbacked.entity.ChatMessage;
import org.example.angelbacked.mapper.ChatSessionMapper;
import org.example.angelbacked.mapper.ChatMessageMapper;
import org.example.angelbacked.service.ChatService;
import org.example.angelbacked.util.AIUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private AIUtil aiUtil;
    
    @Override
    public String chatWithAI(String message, String sessionId, Integer userId) {
        // 获取会话历史记录作为上下文
        List<Map<String, String>> context = getSessionContext(sessionId);
        
        // 调用AI工具获取回复
        String aiResponse = aiUtil.chatWithAI(message, sessionId, context);
        
        // 保存用户消息和AI回复到数据库
        saveMessageToDatabase(sessionId, message, "user");
        saveMessageToDatabase(sessionId, aiResponse, "assistant");
        
        return aiResponse;
    }
    
    @Override
    public ChatSession createSession(Integer userId) {
        ChatSession session = new ChatSession();
        session.setUserId(String.valueOf(userId));
        session.setTitle("新会话");
        session.setStatus(0); // 0表示活跃状态
        chatSessionMapper.insert(session);
        return session;
    }
    
    @Override
    public List<ChatSession> getSessions(Integer userId) {
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("updated_at");
        return chatSessionMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<ChatMessage> getSessionHistory(String sessionId) {
        // 先获取session的数据库ID
        QueryWrapper<ChatSession> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.eq("id", sessionId);
        ChatSession session = chatSessionMapper.selectOne(sessionQueryWrapper);
        
        if (session != null) {
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("session_id", session.getId());
            queryWrapper.orderByAsc("sequence");
            return chatMessageMapper.selectList(queryWrapper);
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public boolean deleteSession(String sessionId, Integer userId) {
        QueryWrapper<ChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", sessionId);
        queryWrapper.eq("user_id", userId);
        
        ChatSession session = chatSessionMapper.selectOne(queryWrapper);
        if (session != null) {
            // 删除会话本身 (由于设置了外键CASCADE，消息会自动删除)
            chatSessionMapper.deleteById(session.getId());
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> getQuickReplies() {
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("你今天过得怎么样？");
        quickReplies.add("有什么烦心事想聊聊吗？");
        quickReplies.add("有什么开心的事情分享吗？");
        quickReplies.add("最近有什么压力吗？");
        quickReplies.add("你对未来的计划是什么？");
        return quickReplies;
    }
    
    @Override
    public Object getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("welcomeMessage", "你好！我是你的心理助手，有什么我可以帮你的吗？");
        config.put("botName", "心灵树洞");
        config.put("botAvatar", "");
        return config;
    }
    
    /**
     * 获取会话上下文（最近几条对话）
     */
    private List<Map<String, String>> getSessionContext(String sessionId) {
        List<ChatMessage> historyMessages = getSessionHistory(sessionId);
        
        // 只保留最近的10条消息作为上下文（避免token过多）
        int contextSize = Math.min(historyMessages.size(), 10);
        List<Map<String, String>> context = new ArrayList<>();
        
        // 从最新的消息开始往前数contextSize条
        for (int i = Math.max(0, historyMessages.size() - contextSize); i < historyMessages.size(); i++) {
            ChatMessage msg = historyMessages.get(i);
            Map<String, String> messageMap = new HashMap<>();
            
            messageMap.put("role", msg.getRole());
            messageMap.put("content", msg.getContent());
            
            context.add(messageMap);
        }
        
        return context;
    }
    
    /**
     * 保存消息到数据库
     */
    private void saveMessageToDatabase(String sessionId, String content, String role) {
        // 获取session的数据库ID
        QueryWrapper<ChatSession> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.eq("id", sessionId);
        ChatSession session = chatSessionMapper.selectOne(sessionQueryWrapper);
        
        if (session != null) {
            // 计算消息序号(sequence)
            QueryWrapper<ChatMessage> messageQueryWrapper = new QueryWrapper<>();
            messageQueryWrapper.eq("session_id", session.getId());
            long messageCount = chatMessageMapper.selectCount(messageQueryWrapper); // 更改为long类型
            
            ChatMessage message = new ChatMessage();
            message.setSessionId(session.getId());
            message.setContent(content);
            message.setRole(role);
            message.setSequence((int) (messageCount + 1)); // 序号从1开始，强制转换为int
            
            chatMessageMapper.insert(message);
            
            // 更新会话的更新时间 (由数据库自动处理)
            // 更新会话标题为第一条消息的前20个字符
            if (messageCount == 0) { // 第一条消息
                String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
                session.setTitle(title);
                chatSessionMapper.updateById(session);
            }
        }
    }
}