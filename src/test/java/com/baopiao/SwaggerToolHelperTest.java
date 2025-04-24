package com.baopiao;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.json.JSONUtil;
import com.baopiao.specification.config.SwaggerToolConfig;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.json.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutor;
import org.checkerframework.common.subtyping.qual.Bottom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class SwaggerToolHelperTest {


    @Test
    public void testSwaggerTools() {
        String s = FileUtil.readUtf8String("swagger.json");

        Map<ToolSpecification, ToolExecutor> toolSpecificationToolExecutorMap = SwaggerToolHelper.formToolMapByJson(SwaggerToolConfig.builder()
                .json(s)
                .requestUrl("http://localhost:8023")
                .build());
        Assert.assertNotNull(toolSpecificationToolExecutorMap);
        Assert.assertNotNull(toolSpecificationToolExecutorMap.keySet());

        Map<String, ToolSpecification> specificationMap = toolSpecificationToolExecutorMap.keySet().stream().collect(Collectors.toMap(a -> a.name(), b -> b));
        ToolSpecification toolSpecification = specificationMap.get("handleUserInputUsingGET");
        Assert.assertNotNull(toolSpecification);
        Assert.assertNotNull(toolSpecification.parameters());
        Assert.assertNotNull(toolSpecification.parameters().properties());
        Assert.assertEquals(1, toolSpecification.parameters().properties().size());
        JsonSchemaElement id = toolSpecification.parameters().properties().get("id");
        Assert.assertNotNull(id);
        Assert.assertTrue(id instanceof JsonStringSchema);
        Assert.assertEquals("用户id", ((JsonStringSchema) id).description());


        ToolSpecification getUserToolSpecification = specificationMap.get("getUserInfoUsingGET");
        Assert.assertNotNull(getUserToolSpecification);
        Assert.assertNotNull(getUserToolSpecification.parameters());
        Assert.assertNotNull(getUserToolSpecification.parameters().properties());
        Assert.assertEquals(1, getUserToolSpecification.parameters().properties().size());
        JsonSchemaElement id1 = getUserToolSpecification.parameters().properties().get("id");
        Assert.assertNotNull(id1);
        Assert.assertTrue(id1 instanceof JsonStringSchema);
        Assert.assertEquals("用户id", ((JsonStringSchema) id1).description());


        ToolSpecification handleUserInputUsingPOSTToolSpecification = specificationMap.get("handleUserInputUsingPOST");
        Assert.assertNotNull(handleUserInputUsingPOSTToolSpecification);
        Assert.assertNotNull(handleUserInputUsingPOSTToolSpecification.parameters());
        Map<String, JsonSchemaElement> properties = handleUserInputUsingPOSTToolSpecification.parameters().properties();
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.size());

        // 测试Body
        JsonSchemaElement userInputBean = properties.get("UserInputBean");
        Assert.assertNotNull(userInputBean);
        Assert.assertTrue(userInputBean instanceof JsonObjectSchema);
        Assert.assertNotNull(((JsonObjectSchema) userInputBean).required());
        Assert.assertEquals(1, ((JsonObjectSchema) userInputBean).required().size());
        Assert.assertEquals("num", ((JsonObjectSchema) userInputBean).required().get(0));
        Assert.assertEquals("用户信息", ((JsonObjectSchema) userInputBean).description());
        Map<String, JsonSchemaElement> properties1 = ((JsonObjectSchema) userInputBean).properties();
        Assert.assertEquals(12, properties1.size());

        JsonSchemaElement addressInput = properties1.get("addressInput");
        Assert.assertNotNull(addressInput);
        Assert.assertTrue(addressInput instanceof JsonStringSchema);
        Assert.assertEquals("地址", ((JsonStringSchema) addressInput).description());

//        JsonSchemaElement ageInput = properties1.get("byteArrayInput");
//        Assert.assertNotNull(ageInput);
//        Assert.assertTrue(ageInput instanceof JsonArraySchema);
//        Assert.assertEquals("车牌号码", ((JsonStringSchema) ageInput).description());
//        Assert.assertTrue(((JsonArraySchema) ageInput).items() instanceof  JsonStringSchema);

    }
}