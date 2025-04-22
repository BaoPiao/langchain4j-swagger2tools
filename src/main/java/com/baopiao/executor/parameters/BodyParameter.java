package com.baopiao.executor.parameters;

import cn.hutool.json.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyParameter {
    private JSON bodyJson;
}
