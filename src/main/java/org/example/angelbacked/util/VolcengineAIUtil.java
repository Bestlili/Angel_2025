package org.example.angelbacked.util;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class VolcengineAIUtil {

    @Value("${ai.volcengine.api.key:}")
    private String apiKey;

    @Value("${ai.volcengine.base.url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${ai.volcengine.model:doubao-seed-1-6-251015}")
    private String model;

    private ArkService service;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
            Dispatcher dispatcher = new Dispatcher();
            service = ArkService.builder()
                    .dispatcher(dispatcher)
                    .connectionPool(connectionPool)
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .build();
        }
    }

    @PreDestroy
    public void destroy() {
        if (service != null) {
            service.shutdownExecutor();
        }
    }

    /**
     * 使用火山引擎模型进行聊天
     *
     * @param message 用户消息
     * @param context 对话上下文
     * @return AI回复内容
     */
    public String chatWithAI(String message, List<java.util.Map<String, String>> context) {
        if (service == null) {
            throw new IllegalStateException("Volcengine AI service not initialized. Check API key configuration.");
        }

        try {
            final List<ChatMessage> messages = new ArrayList<>();

            // 添加系统角色消息（设定AI的行为）
            ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("你是一位专业的心理咨询师，你的名字叫心灵树洞。你的职责是倾听用户的困扰，给予温暖的回应和适当的建议。请用温和、理解和支持的语气与用户交流，避免评判性的语言。如果用户有严重的心理问题，请建议他们寻求专业帮助。")
                    .build();
            messages.add(systemMessage);

            // 添加历史上下文
            if (context != null && !context.isEmpty()) {
                for (java.util.Map<String, String> ctx : context) {
                    String role = ctx.get("role");
                    String content = ctx.get("content");

                    ChatMessageRole chatRole = ChatMessageRole.USER;
                    if ("assistant".equals(role)) {
                        chatRole = ChatMessageRole.ASSISTANT;
                    } else if ("system".equals(role)) {
                        chatRole = ChatMessageRole.SYSTEM;
                    }

                    ChatMessage historyMessage = ChatMessage.builder()
                            .role(chatRole)
                            .content(content)
                            .build();
                    messages.add(historyMessage);
                }
            }

            // 添加当前用户消息
            final ChatMessage userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(message)
                    .build();
            messages.add(userMessage);

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .reasoningEffort("medium")
                    .build();

            com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult result =
                    service.createChatCompletion(chatCompletionRequest);

            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                return result.getChoices().get(0).getMessage().stringContent();
            }

            return "抱歉，我没有理解你的意思。";
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我现在无法回答，请稍后再试。";
        }
    }

    /**
     * 流式聊天接口
     *
     * @param message 用户消息
     * @param context 对话上下文
     * @return 流式响应结果
     */
    public String streamChatWithAI(String message, List<java.util.Map<String, String>> context) {
        if (service == null) {
            throw new IllegalStateException("Volcengine AI service not initialized. Check API key configuration.");
        }

        try {
            final List<ChatMessage> messages = new ArrayList<>();

            // 添加系统角色消息（设定AI的行为）
            ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("你是一位专业的心理咨询师，你的名字叫心灵树洞。你的职责是倾听用户的困扰，给予温暖的回应和适当的建议。请用温和、理解和支持的语气与用户交流，避免评判性的语言。如果用户有严重的心理问题，请建议他们寻求专业帮助。")
                    .build();
            messages.add(systemMessage);

            // 添加历史上下文
            if (context != null && !context.isEmpty()) {
                for (java.util.Map<String, String> ctx : context) {
                    String role = ctx.get("role");
                    String content = ctx.get("content");

                    ChatMessageRole chatRole = ChatMessageRole.USER;
                    if ("assistant".equals(role)) {
                        chatRole = ChatMessageRole.ASSISTANT;
                    } else if ("system".equals(role)) {
                        chatRole = ChatMessageRole.SYSTEM;
                    }

                    ChatMessage historyMessage = ChatMessage.builder()
                            .role(chatRole)
                            .content(content)
                            .build();
                    messages.add(historyMessage);
                }
            }

            // 添加当前用户消息
            final ChatMessage userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(message)
                    .build();
            messages.add(userMessage);

            ChatCompletionRequest streamChatCompletionRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .reasoningEffort("medium")
                    .build();

            StringBuilder result = new StringBuilder();
            service.streamChatCompletion(streamChatCompletionRequest)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(
                            choice -> {
                                if (choice.getChoices().size() > 0) {
                                    result.append(choice.getChoices().get(0).getMessage().stringContent());
                                }
                            }
                    );

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我现在无法回答，请稍后再试。";
        }
    }
}