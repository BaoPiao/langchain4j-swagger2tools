package com.baopiao;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.json.JSONUtil;
import com.baopiao.specification.config.SwaggerToolConfig;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
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

    private String apiKey;

    @Before
    public void setUp() throws Exception {
        apiKey = System.getenv("api-key");

    }

    @Test
    public void formToolMapByUrl() {
    }

    @Test
    public void formToolMapByJson() {
        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com")
                .parallelToolCalls(Boolean.FALSE)
                .modelName("deepseek-chat")
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .build();
        ChatAssistant ChatAssistant = AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(model)
                .tools().build();

//        String chat = ChatAssistant.chat("帮我存储以下小明的信息" +
//                "该用户拥有 3 辆车，且确认其拥有车辆（hasCar 为 true）。\n" +
//                "用户的身高为 175.25 米，出生日期为 1990-05-20。\n" +
//                "用户的联系方式包括邮箱地址 randomuser@example.com 和电话号码 +19876543210。\n" +
//                "用户的居住地址为 456 Random St, Town, Country。\n" +
//                "用户的兴趣爱好包括 阅读 和 旅行，以列表形式存储。\n" +
//                "如果用户拥有车辆，则车辆的行驶公里数为 5432167890123 公里。\n" +
//                "车辆的车牌区域号为 B，车牌号码由字节数组表示，具体内容为 6,6,1,2,3。\n" +
//                "通过这些信息，可以全面了解这位用户的个人资料及其与车辆相关的详细情况。");
//        System.out.println(chat);
    }

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

        System.out.println(toolSpecificationToolExecutorMap);
    }
}