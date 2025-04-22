package com.baopiao;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ChatAssistant {

    @SystemMessage("你是一个智能助手")
    String chat(@UserMessage("用户描述") String userMessage);
}
