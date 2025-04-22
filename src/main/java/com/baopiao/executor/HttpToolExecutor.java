package com.baopiao.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baopiao.executor.parameters.*;
import com.baopiao.specification.config.SwaggerToolConfig;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.ToolExecutor;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HttpToolExecutor implements ToolExecutor {

    private Map.Entry<String, PathItem> pathItemEntry;
    private SwaggerToolConfig config;

    public HttpToolExecutor(Map.Entry<String, PathItem> pathItemEntry, SwaggerToolConfig config) {
        log.debug("--------------------");
        log.debug(JSONUtil.toJsonStr(pathItemEntry));
        log.debug("--------------------");
        log.debug(JSONUtil.toJsonStr(config));
        log.debug("--------------------");

        this.pathItemEntry = pathItemEntry;
        this.config = config;
    }

    @Override
    public String execute(ToolExecutionRequest toolExecutionRequest, Object memoryId) {

        JSONObject entries = JSONUtil.parseObj(toolExecutionRequest.arguments());
        PathItem value = pathItemEntry.getValue();
        Map<String, Parameter> parameterMap = new HashMap<>();
        Request request = null;
        Operation operation = null;
        if (ObjectUtil.isNotEmpty(value.getPost())) {
            operation = value.getPost();
            request = new PostRequest();
            request.setUrl(pathItemEntry.getKey());
            List<Parameter> parameters = value.getPost().getParameters();
            if (CollUtil.isNotEmpty(parameters)) {
                parameterMap = parameters.stream().collect(Collectors.toMap(a -> a.getName(), b -> b));
            }
        } else if (ObjectUtil.isNotEmpty(value.getGet())) {
            operation = value.getGet();
            request = new GetRequest();
            request.setUrl(pathItemEntry.getKey());

            List<Parameter> parameters = value.getGet().getParameters();
            if (CollUtil.isNotEmpty(parameters)) {
                parameterMap = parameters.stream().collect(Collectors.toMap(a -> a.getName(), b -> b));
            }
        } else {
            log.warn("暂不支持当前类型{}", value);
            return "暂不支持当前类型";
        }

        String[] bodyName = new String[1];
        Optional.ofNullable(operation)
                .map(o -> o.getRequestBody())
                .map(a -> a.getContent())
                .map(b -> b.get("application/json"))
                .map(c -> c.getSchema())
                .map(d -> d.get$ref()).ifPresent(e -> {
            String[] split = e.split("\\/");
            bodyName[0] = split[split.length - 1];
        });


        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            log.debug("key:{},value:{}", entry.getKey(), entry.getValue());
            if (StrUtil.equals(entry.getKey(), bodyName[0])) {
                request.setBodyParameter(BodyParameter.builder().bodyJson(JSONUtil.parse(entry.getValue())).build());
                continue;
            }
            if (parameterMap.containsKey(entry.getKey())) {
                Parameter parameter = parameterMap.get(entry.getKey());
                if (StrUtil.equals("query", parameter.getIn())) {
                    List<RequestParameter> requestParameter = CollUtil.isEmpty(request.getRequestParameter()) ? new ArrayList<>() : request.getRequestParameter();
                    requestParameter.add(RequestParameter.builder()
                            .name(entry.getKey())
                            .bodyJson(JSONUtil.createObj().set(entry.getKey(), entry.getValue()))
                            .build());
                    request.setRequestParameter(requestParameter);
                } else if (StrUtil.equals("path", parameter.getIn())) {
                    List<PathParameter> pathParameter = CollUtil.isEmpty(request.getPathParameter()) ? new ArrayList<>() : request.getPathParameter();
                    pathParameter.add(PathParameter.builder()
                            .name(entry.getKey())
                            .bodyJson(JSONUtil.createObj().set(entry.getKey(), entry.getValue()))
                            .build());
                    request.setPathParameter(
                            pathParameter
                    );
                }
            }
        }

        return Optional
                .ofNullable(config.getExecutor())
                .orElseGet(() -> new DefaultHttpExecutor())
                .execute(config, request);
    }


}
