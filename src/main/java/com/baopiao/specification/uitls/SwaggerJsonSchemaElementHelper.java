package com.baopiao.specification.uitls;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import dev.langchain4j.model.chat.request.json.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static dev.langchain4j.internal.Utils.generateUUIDFrom;

@Slf4j
public class SwaggerJsonSchemaElementHelper {

    public static RefNameAndJsonSchemaElement JsonSchemaElementFormForBody(Schema schema,
                                                                           OpenAPI openAPI,
                                                                           Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited) {
        String ref = schema.get$ref();
        if (StrUtil.isNotEmpty(ref)) {
            SchemaAndRedName refSchema = getRefSchema(ref, openAPI);
            if (ObjectUtil.isEmpty(refSchema) || ObjectUtil.isEmpty(refSchema.schema())) {
                log.warn("查找引用类型失败！");
                return null;
            }
            return new RefNameAndJsonSchemaElement(refSchema.refName(),
                    JsonSchemaElementForm(new NameAndRefNameAndDescription(null, refSchema.refName(), null), refSchema.schema(), openAPI, visited, Boolean.TRUE)
            );
        } else {
            //TODO 非引用类型
            return null;
        }
    }


    private static SchemaAndRedName getRefSchema(String ref, OpenAPI openAPI) {

        if (ref.contains("components/schemas")) {
            String[] split = ref.split("/");
            String refName = split[split.length - 1];
            return new SchemaAndRedName(openAPI.getComponents().getSchemas().get(refName), refName);
        } else {
            log.error("应用类型解析错误！");
            return null;
        }
    }

    /**
     * @param nameRecord 1:name 2:refName 3:des
     * @param schema
     * @param openAPI
     * @param visited
     * @return
     */
    public static JsonSchemaElement JsonSchemaElementForm(NameAndRefNameAndDescription nameRecord,
                                                          Schema schema,
                                                          OpenAPI openAPI,
                                                          Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited,
                                                          Boolean setDefinitions) {
        if (StrUtil.isNotEmpty(schema.get$ref())) {
            String ref = schema.get$ref();
            if (ref.contains("components/schemas")) {
                String[] split = ref.split("/");
                String refName = split[split.length - 1];
                Schema schema1 = openAPI.getComponents().getSchemas().get(refName);
                return JsonSchemaElementForm(new NameAndRefNameAndDescription(nameRecord.name(), refName, null), schema1, openAPI, visited, setDefinitions);
            } else {
                //TODO 其它应用未知！
                return null;
            }
        }
        //描述这里分为两种情况，第一种属于param解析来的，第二种是body的param解析来的，如果调用者传入，使用调用者，否则取schema自己的
        String description = null == nameRecord.description() ? schema.getDescription() : nameRecord.description();

        if (schema instanceof ObjectSchema) {
            return getJsonObjectSchema(nameRecord, schema, openAPI, visited, description, setDefinitions);
        } else if (schema instanceof ArraySchema) {
            return jsonArraySchemaElementFrom(description, schema, openAPI, visited, setDefinitions);
        } else {
            return jsonSchemaForm(description, schema);
        }
    }

    @Nullable
    private static JsonSchemaElement getJsonObjectSchema(NameAndRefNameAndDescription nameRecord,
                                                         Schema schema,
                                                         OpenAPI openAPI,
                                                         Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited,
                                                         String description,
                                                         Boolean setDefinitions) {
        Map<String, Schema> properties = schema.getProperties();
        if (visited.containsKey(nameRecord.refName())) {
            JsonSchemaElementHelper.VisitedClassMetadata visitedClassMetadata = visited.get(nameRecord.refName());
            JsonSchemaElement jsonSchemaElement = visitedClassMetadata.jsonSchemaElement;
            if (jsonSchemaElement instanceof JsonReferenceSchema) {
                visitedClassMetadata.recursionDetected = true;
            }
            return jsonSchemaElement;
        }
        String reference = generateUUIDFrom(nameRecord.refName());
        JsonReferenceSchema jsonReferenceSchema =
                JsonReferenceSchema.builder().reference(reference).build();
        visited.put(nameRecord.refName(), new JsonSchemaElementHelper.VisitedClassMetadata(jsonReferenceSchema, reference, false));

        JsonObjectSchema.Builder builder1 =
                JsonObjectSchema.builder()
                        .required(schema.getRequired())
                        .description(description);
        setProperties(openAPI, properties, builder1, visited, Boolean.FALSE);

        visited.get(nameRecord.refName()).jsonSchemaElement = builder1.build();

        if (setDefinitions) {
            Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();
            if (CollectionUtil.isNotEmpty(visited)) {
                visited.forEach((s, visitedClassMetadata) -> {
                    if (visitedClassMetadata.recursionDetected) {
                        definitions.put(visitedClassMetadata.reference, visitedClassMetadata.jsonSchemaElement);
                    }
                });
            }
            if (!definitions.isEmpty()) {
                builder1.definitions(definitions);
            }
        }
        return builder1.build();

    }

    private static void setProperties(OpenAPI openAPI, Map<String, Schema> properties,
                                      JsonObjectSchema.Builder builder1,
                                      Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited,
                                      Boolean setDefinitions) {
        if (CollectionUtil.isNotEmpty(properties)) {
            for (Map.Entry<String, Schema> stringSchemaEntry : properties.entrySet()) {
                Schema schemaElement = stringSchemaEntry.getValue();
                builder1.addProperty(
                        stringSchemaEntry.getKey(),
                        JsonSchemaElementForm(new NameAndRefNameAndDescription(stringSchemaEntry.getKey(), null, null),
                                schemaElement,
                                openAPI,
                                visited, setDefinitions));
            }
        }
    }

    private static JsonSchemaElement jsonSchemaForm(String description, Schema schema) {
        if (schema instanceof StringSchema) {
            return JsonStringSchema.builder().description(description).build();
        } else if (schema instanceof BooleanSchema) {
            return JsonBooleanSchema.builder().description(description).build();
        } else if (schema instanceof IntegerSchema) {
            return JsonIntegerSchema.builder().description(description).build();
        } else if (schema instanceof BinarySchema) {
            return JsonStringSchema.builder().description(description).build();
        } else if (schema instanceof NumberSchema) {
            return JsonNumberSchema.builder().description(description).build();
        } else if (schema instanceof DateSchema) {
            return JsonStringSchema.builder().description(description).build();
        } else {
            return JsonStringSchema.builder().description(description).build();
        }
    }

    @Nullable
    private static JsonSchemaElement jsonArraySchemaElementFrom(String description,
                                                                Schema schema,
                                                                OpenAPI openAPI,
                                                                Map<String, JsonSchemaElementHelper.VisitedClassMetadata> visited,
                                                                Boolean setDefinitions) {
        if (ObjectUtil.isNotEmpty(schema.getItems())) {
            Schema items = schema.getItems();
            JsonArraySchema.Builder builder1 = JsonArraySchema.builder();
            JsonSchemaElement jsonSchemaElement =
                    JsonSchemaElementForm(new NameAndRefNameAndDescription(null, null, description),
                            items,
                            openAPI,
                            visited, setDefinitions);
            builder1.description(description).items(jsonSchemaElement);
            return builder1.build();
        } else {
            return JsonArraySchema.builder().description(description).build();
        }
    }
}
