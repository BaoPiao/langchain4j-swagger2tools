package com.baopiao.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baopiao.executor.parameters.GetRequest;
import com.baopiao.executor.parameters.PostRequest;
import com.baopiao.executor.parameters.Request;
import com.baopiao.specification.config.SwaggerToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class DefaultHttpExecutor implements Executor {


    public String execute(SwaggerToolConfig swaggerToolConfig, Request request) {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder httpBuilder = HttpRequest.newBuilder().headers("Content-Type", "application/json");
        String requestUrl = swaggerToolConfig.getRequestUrl() + request.getUrl();

        try {
            URIBuilder uriBuilder = getUriBuilder(request, requestUrl);
            httpBuilder.uri(uriBuilder.build());
        } catch (URISyntaxException e) {
            log.error("构建请求地址失败{} {}", requestUrl, request, e);
            return "构建请求地址失败" + e.getMessage();
        }


        try {
            if (request instanceof GetRequest) {
                HttpRequest build = httpBuilder.GET().build();
                return client.send(build, HttpResponse.BodyHandlers.ofString()).body();

            } else if (request instanceof PostRequest) {
                String requestBody = new String();
                if (ObjectUtil.isNotEmpty(request.getBodyParameter())) {
                    requestBody = request.getBodyParameter().getBodyJson().toJSONString(0);
                }
                HttpRequest build = httpBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

                return client.send(build, HttpResponse.BodyHandlers.ofString()).body();

            } else {
                return "当前请求类型不支持";
            }
        } catch (Exception e) {
            return "请求失败: " + e.getMessage();
        }
    }

    @NotNull
    private URIBuilder getUriBuilder(Request request, String requestUrl) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(requestUrl);
        // 处理路径参数
        if (CollUtil.isNotEmpty(request.getPathParameter())) {
            request.getPathParameter().forEach(param -> {
                if (StrUtil.isNotBlank(param.getName())) {
                    Optional.ofNullable(param.getBodyJson()).ifPresent(json -> {
                        Map<String, Object> paramMap = json.toBean(Map.class);
                        paramMap.forEach((key, value) -> {
                            uriBuilder.addParameter(key, String.valueOf(value));
                        });
                    });
                }
            });
        }

        // 处理请求参数，直接添加到URL中
        if (CollUtil.isNotEmpty(request.getRequestParameter())) {
            request.getRequestParameter().forEach(param -> {
                if (StrUtil.isNotBlank(param.getName())) {
                    Optional.ofNullable(param.getBodyJson()).ifPresent(json -> {
                        Map<String, Object> paramMap = json.toBean(Map.class);
                        paramMap.forEach((key, value) -> {
                            uriBuilder.addParameter(key, String.valueOf(value));
                        });
                    });
                }
            });
        }
        return uriBuilder;
    }
}
