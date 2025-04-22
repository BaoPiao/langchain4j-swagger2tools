package com.baopiao.specification.uitls;

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;

public record RefNameAndJsonSchemaElement(String refName, JsonSchemaElement jsonSchemaElement) {
}
