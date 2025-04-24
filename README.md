# LangChain4j-Swagger2Tools

本工具用于将swagger文档直接转换为LangChain4j的Tools对象，0改动将接口整合到大模型中！

## 使用方法
1. 引入依赖
```xml
<dependency>
    <groupId>io.github.baopiao</groupId>
    <artifactId>langchain4j-swagger2tools</artifactId>
    <version>1.1</version>
</dependency>
```
2. 使用SwaggerToolHelper将Swagger转换为Tools
```java
ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey("sk-**") // 你的api key
            .baseUrl("https://api.deepseek.com").parallelToolCalls(Boolean.FALSE)
            .modelName("deepseek-chat").logRequests(Boolean.TRUE).logResponses(Boolean.TRUE)
            .build();
String swaggerV2JsonUrl = "http://localhost:8023/v2/api-docs";//你的swagger文档地址
NGAssistant NGAssistant = AiServices.builder(NGAssistant.class)
        .chatLanguageModel(model)
        .tools(SwaggerToolHelper.formToolMapByUrl(SwaggerToolConfig
                .builder()
                .url(swaggerV2JsonUrl)
                .requestUrl("http://localhost:8023")//实际请求接口的地址
                .build())).build();
```

