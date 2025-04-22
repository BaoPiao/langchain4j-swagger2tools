package com.baopiao.executor.parameters;

import cn.hutool.json.JSON;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import lombok.Data;

@Data
public class ParseParameter {
    private Parameter parameter;
    private RequestBody requestBody;
    private JSON bodyJson;
}
