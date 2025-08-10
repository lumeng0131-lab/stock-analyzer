package com.stockanalyzer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 提供JSON序列化和反序列化的常用功能
 */
public class JsonUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 配置ObjectMapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将对象转换为格式化的JSON字符串
     * 
     * @param obj 对象
     * @return 格式化的JSON字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to pretty JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || clazz == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.error("Error converting JSON to object: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为复杂类型对象
     * 
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json) || typeReference == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            logger.error("Error converting JSON to complex object: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为Map
     * 
     * @param json JSON字符串
     * @return Map对象
     */
    public static Map<String, Object> toMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            logger.error("Error converting JSON to Map: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为List
     * 
     * @param json JSON字符串
     * @param clazz 列表元素类型
     * @param <T> 泛型类型
     * @return List对象
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || clazz == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            logger.error("Error converting JSON to List: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 将JSON字符串解析为JsonNode
     * 
     * @param json JSON字符串
     * @return JsonNode对象
     */
    public static JsonNode parseJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JsonNode中获取文本值
     * 
     * @param node JsonNode
     * @param fieldName 字段名
     * @return 文本值，如果字段不存在或不是文本则返回null
     */
    public static String getTextValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isTextual() ? fieldNode.asText() : null;
    }
    
    /**
     * 从JsonNode中获取数值
     * 
     * @param node JsonNode
     * @param fieldName 字段名
     * @return 数值，如果字段不存在或不是数值则返回null
     */
    public static Double getDoubleValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNumber() ? fieldNode.asDouble() : null;
    }
    
    /**
     * 从JsonNode中获取整数值
     * 
     * @param node JsonNode
     * @param fieldName 字段名
     * @return 整数值，如果字段不存在或不是整数则返回null
     */
    public static Integer getIntValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isInt() ? fieldNode.asInt() : null;
    }
    
    /**
     * 从JsonNode中获取布尔值
     * 
     * @param node JsonNode
     * @param fieldName 字段名
     * @return 布尔值，如果字段不存在或不是布尔值则返回null
     */
    public static Boolean getBooleanValue(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isBoolean() ? fieldNode.asBoolean() : null;
    }
    
    /**
     * 获取ObjectMapper实例
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}