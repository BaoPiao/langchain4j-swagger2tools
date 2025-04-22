package com.baopiao.executor;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;

public class LocalToolExecutor implements ToolExecutor {
    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {
        return null;
    }
}
