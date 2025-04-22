package com.baopiao.executor.parameters;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Request {
    private String url;
    private BodyParameter bodyParameter;
    private List<PathParameter> pathParameter;
    private List<RequestParameter> requestParameter;
}
