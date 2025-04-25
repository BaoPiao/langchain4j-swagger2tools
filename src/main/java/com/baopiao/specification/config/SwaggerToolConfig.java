package com.baopiao.specification.config;

import com.baopiao.executor.Executor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.Header;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwaggerToolConfig {

    /**
     * 请求swagger的地址
     */
    private String url;

    /**
     * swagger的json格式文档
     */
    private String json;

    /**
     * 执行请求的地址
     */
    private String requestUrl;

    /**
     * 正则匹配规则，为空表示匹配所有
     */
    private String urlRegex;

    /**
     * 自定义执行类 为空使用默认实现
     */
    private Executor executor;

    /**
     * 设置请求头
     */
    private Map<String, String> headerMap;


}
