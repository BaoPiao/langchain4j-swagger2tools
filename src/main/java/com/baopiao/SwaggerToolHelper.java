package com.baopiao;

import cn.hutool.core.util.ObjectUtil;
import com.baopiao.executor.HttpToolExecutor;
import com.baopiao.specification.config.SwaggerToolConfig;
import com.baopiao.specification.uitls.NameAndRefNameAndDescription;
import com.baopiao.specification.uitls.RefNameAndJsonSchemaElement;
import com.baopiao.specification.uitls.SwaggerJsonSchemaElementHelper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonSchemaElementHelper;
import dev.langchain4j.service.tool.ToolExecutor;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class SwaggerToolHelper {

    public static Map<ToolSpecification, ToolExecutor> formToolMapByUrl(SwaggerToolConfig swaggerToolConfig) {
        Map<ToolSpecification, ToolExecutor> toolSpecificationToolExecutorMap = new HashMap<>();

        SwaggerParseResult result = new OpenAPIParser()
                .readLocation(swaggerToolConfig.getUrl(), null, null);

        Optional.ofNullable(result.getOpenAPI()).ifPresent(openAPI -> {
            for (Map.Entry<String, PathItem> stringPathItemEntry : openAPI.getPaths().entrySet()) {
                // 使用正则表达式匹配路径
                if (swaggerToolConfig.getUrlRegex() == null ||
                        stringPathItemEntry.getKey().matches(swaggerToolConfig.getUrlRegex())) {
                    ToolSpecification toolSpecification = toolSpecificationForm(stringPathItemEntry, openAPI);
                    toolSpecificationToolExecutorMap.put(toolSpecification, new HttpToolExecutor(stringPathItemEntry, swaggerToolConfig));
                }
            }
        });
        return toolSpecificationToolExecutorMap;
    }

    public static Map<ToolSpecification, ToolExecutor> formToolMapByJson(SwaggerToolConfig swaggerToolConfig) {

        Map<ToolSpecification, ToolExecutor> toolSpecificationToolExecutorMap = new HashMap<>();

        SwaggerParseResult result = new OpenAPIParser()
                .readContents(swaggerToolConfig.getJson(), null, null);

        Optional.ofNullable(result.getOpenAPI()).ifPresent(openAPI -> {
            for (Map.Entry<String, PathItem> stringPathItemEntry : openAPI.getPaths().entrySet()) {
                if (swaggerToolConfig.getUrlRegex() == null ||
                        stringPathItemEntry.getKey().matches(swaggerToolConfig.getUrlRegex())) {
                    ToolSpecification toolSpecification = toolSpecificationForm(stringPathItemEntry, openAPI);
                    toolSpecificationToolExecutorMap.put(toolSpecification, new HttpToolExecutor(stringPathItemEntry, swaggerToolConfig));
                }
            }
        });
        return toolSpecificationToolExecutorMap;
    }


    private static ToolSpecification toolSpecificationForm(Map.Entry<String, PathItem> pathItemMap, OpenAPI openAPI) {

        PathItem pathItem = pathItemMap.getValue();
        ToolSpecification.Builder toolSpecBuilder = ToolSpecification.builder();

        ArrayList<String> requiredNames = new ArrayList<>();
        Operation operation;
        if (ObjectUtil.isNotEmpty(pathItem.getPost())) {
            operation = pathItem.getPost();
        } else if (ObjectUtil.isNotEmpty(pathItem.getGet())) {
            operation = pathItem.getGet();
        } else {
            log.warn("暂不支持当前类型{}", pathItem);
            return null;
        }

        toolSpecBuilder.description(operation.getSummary());
        toolSpecBuilder.name(operation.getOperationId());

        JsonObjectSchema.Builder allBuilder = JsonObjectSchema.builder();
        Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited = new LinkedHashMap<>();
        //增加body
        Optional.ofNullable(operation.getRequestBody()).ifPresent(
                body -> {
                    body.getContent().forEach((name, content) -> {
                        Schema schema = content.getSchema();
                        if (ObjectUtil.isNotEmpty(schema)) {
                            RefNameAndJsonSchemaElement elementRecord = SwaggerJsonSchemaElementHelper.JsonSchemaElementFormForBody(schema, openAPI, visited);
                            if (ObjectUtil.isNotEmpty(elementRecord)) {
                                allBuilder.addProperty(elementRecord.refName(), elementRecord.jsonSchemaElement());
                                if (body.getRequired()) {
                                    requiredNames.add(elementRecord.refName());
                                }
                            }
                        }
                    });
                });

        //增加params
        Optional.ofNullable(operation.getParameters()).ifPresent(parameters ->
                parameters.forEach(parameter -> {
                    if (Boolean.TRUE.equals(parameter.getRequired())) {
                        requiredNames.add(parameter.getName());
                    }
                    if (ObjectUtil.isNotEmpty(parameter.getSchema())) {
                        JsonSchemaElement jsonSchemaElement = SwaggerJsonSchemaElementHelper.JsonSchemaElementForm(new NameAndRefNameAndDescription(parameter.getName(), null, parameter.getDescription()), parameter.getSchema(), openAPI, visited, Boolean.TRUE);
                        if (ObjectUtil.isNotEmpty(jsonSchemaElement)) {
                            allBuilder.addProperty(parameter.getName(), jsonSchemaElement);
                        }
                    }
                })
        );
        ToolSpecification toolSpecification = toolSpecBuilder.parameters(allBuilder.required(requiredNames).build()).build();
        return toolSpecification;
    }


}
