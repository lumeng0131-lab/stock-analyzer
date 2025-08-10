package com.stockanalyzer.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * JsonUtils工具类的单元测试
 */
public class JsonUtilsTest {

    // 测试用的简单对象类
    static class TestObject {
        private String name;
        private int age;
        
        public TestObject() {
        }
        
        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return age == that.age && (name != null ? name.equals(that.name) : that.name == null);
        }
    }
    
    @Test
    public void testToJson() {
        TestObject obj = new TestObject("John", 30);
        String json = JsonUtils.toJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"John\""));
        assertTrue(json.contains("\"age\":30"));
        
        assertNull(JsonUtils.toJson(null));
    }
    
    @Test
    public void testToPrettyJson() {
        TestObject obj = new TestObject("John", 30);
        String json = JsonUtils.toPrettyJson(obj);
        assertNotNull(json);
        assertTrue(json.contains("\"name\" : \"John\""));
        assertTrue(json.contains("\"age\" : 30"));
        
        assertNull(JsonUtils.toPrettyJson(null));
    }
    
    @Test
    public void testFromJson() {
        String json = "{\"name\":\"John\",\"age\":30}";
        TestObject obj = JsonUtils.fromJson(json, TestObject.class);
        assertNotNull(obj);
        assertEquals("John", obj.getName());
        assertEquals(30, obj.getAge());
        
        assertNull(JsonUtils.fromJson(null, TestObject.class));
        assertNull(JsonUtils.fromJson("", TestObject.class));
        assertNull(JsonUtils.fromJson(json, null));
    }
    
    @Test
    public void testFromJsonWithTypeReference() {
        String json = "[{\"name\":\"John\",\"age\":30},{\"name\":\"Jane\",\"age\":25}]";
        List<TestObject> list = JsonUtils.fromJson(json, new TypeReference<List<TestObject>>() {});
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("John", list.get(0).getName());
        assertEquals(30, list.get(0).getAge());
        assertEquals("Jane", list.get(1).getName());
        assertEquals(25, list.get(1).getAge());
        
        assertNull(JsonUtils.fromJson(null, new TypeReference<List<TestObject>>() {}));
        assertNull(JsonUtils.fromJson("", new TypeReference<List<TestObject>>() {}));
        assertNull(JsonUtils.fromJson(json, (TypeReference<List<TestObject>>) null));
    }
    
    @Test
    public void testToMap() {
        String json = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        Map<String, Object> map = JsonUtils.toMap(json);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("John", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals("New York", map.get("city"));
        
        assertNull(JsonUtils.toMap(null));
        assertNull(JsonUtils.toMap(""));
    }
    
    @Test
    public void testToList() {
        String json = "[\"apple\",\"banana\",\"orange\"]";
        List<String> list = JsonUtils.toList(json, String.class);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("apple", list.get(0));
        assertEquals("banana", list.get(1));
        assertEquals("orange", list.get(2));
        
        assertTrue(JsonUtils.toList(null, String.class).isEmpty());
        assertTrue(JsonUtils.toList("", String.class).isEmpty());
        assertTrue(JsonUtils.toList(json, null).isEmpty());
    }
    
    @Test
    public void testParseJson() {
        String json = "{\"name\":\"John\",\"age\":30,\"address\":{\"city\":\"New York\",\"zip\":\"10001\"}}";
        JsonNode node = JsonUtils.parseJson(json);
        assertNotNull(node);
        assertTrue(node.has("name"));
        assertTrue(node.has("age"));
        assertTrue(node.has("address"));
        assertEquals("John", node.get("name").asText());
        assertEquals(30, node.get("age").asInt());
        assertTrue(node.get("address").has("city"));
        assertEquals("New York", node.get("address").get("city").asText());
        
        assertNull(JsonUtils.parseJson(null));
        assertNull(JsonUtils.parseJson(""));
    }
    
    @Test
    public void testGetTextValue() {
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonNode node = JsonUtils.parseJson(json);
        assertEquals("John", JsonUtils.getTextValue(node, "name"));
        assertNull(JsonUtils.getTextValue(node, "city"));
        assertNull(JsonUtils.getTextValue(node, "age")); // age is not a text value
        assertNull(JsonUtils.getTextValue(null, "name"));
    }
    
    @Test
    public void testGetDoubleValue() {
        String json = "{\"price\":19.99,\"name\":\"Book\"}";
        JsonNode node = JsonUtils.parseJson(json);
        assertEquals(19.99, JsonUtils.getDoubleValue(node, "price"), 0.0001);
        assertNull(JsonUtils.getDoubleValue(node, "discount"));
        assertNull(JsonUtils.getDoubleValue(node, "name")); // name is not a number
        assertNull(JsonUtils.getDoubleValue(null, "price"));
    }
    
    @Test
    public void testGetIntValue() {
        String json = "{\"age\":30,\"name\":\"John\"}";
        JsonNode node = JsonUtils.parseJson(json);
        assertEquals(Integer.valueOf(30), JsonUtils.getIntValue(node, "age"));
        assertNull(JsonUtils.getIntValue(node, "year"));
        assertNull(JsonUtils.getIntValue(node, "name")); // name is not an int
        assertNull(JsonUtils.getIntValue(null, "age"));
    }
    
    @Test
    public void testGetBooleanValue() {
        String json = "{\"active\":true,\"name\":\"John\"}";
        JsonNode node = JsonUtils.parseJson(json);
        assertEquals(Boolean.TRUE, JsonUtils.getBooleanValue(node, "active"));
        assertNull(JsonUtils.getBooleanValue(node, "enabled"));
        assertNull(JsonUtils.getBooleanValue(node, "name")); // name is not a boolean
        assertNull(JsonUtils.getBooleanValue(null, "active"));
    }
    
    @Test
    public void testGetObjectMapper() {
        assertNotNull(JsonUtils.getObjectMapper());
    }
}