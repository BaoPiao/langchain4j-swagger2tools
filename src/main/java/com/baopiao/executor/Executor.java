package com.baopiao.executor;

import com.baopiao.executor.parameters.Request;
import com.baopiao.specification.config.SwaggerToolConfig;

public interface Executor {
    String execute(SwaggerToolConfig swaggerToolConfig, Request request);
}
