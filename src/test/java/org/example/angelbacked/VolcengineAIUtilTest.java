package org.example.angelbacked;

import org.example.angelbacked.util.VolcengineAIUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class VolcengineAIUtilTest {

    @Autowired
    private VolcengineAIUtil volcengineAIUtil;

    @Test
    public void testChatWithAI() {
        String message = "你好，今天天气怎么样？";
        List<Map<String, String>> context = new ArrayList<>();
        
        String response = volcengineAIUtil.chatWithAI(message, context);
        
        assertNotNull(response, "AI响应不应为null");
        assertTrue(response.length() > 0, "AI响应不应为空");
        System.out.println("AI响应: " + response);
    }
}