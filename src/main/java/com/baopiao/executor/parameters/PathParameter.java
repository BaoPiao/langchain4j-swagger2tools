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
public class PathParameter {
    private String name;
    private JSON bodyJson;
}
