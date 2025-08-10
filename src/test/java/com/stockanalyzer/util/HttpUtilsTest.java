package com.stockanalyzer.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * HttpUtils工具类的单元测试
 */
public class HttpUtilsTest {

    /**
     * 测试获取响应状态码
     */
    @Test
    public void testGetStatusCode() {
        // 创建模拟的HttpResponse
        HttpResponse response = mock(HttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        
        // 设置模拟对象的行为
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        
        // 测试方法
        int statusCode = HttpUtils.getStatusCode(response);
        assertEquals(HttpStatus.SC_OK, statusCode);
        
        // 测试null参数
        assertEquals(-1, HttpUtils.getStatusCode(null));
    }
    
    /**
     * 注意：以下测试方法需要实际的网络连接，可能会失败
     * 在实际环境中，应该使用模拟的HTTP客户端来测试这些方法
     * 这里仅作为示例，实际使用时可能需要调整
     */
    
    /**
     * 测试GET请求
     * 注意：这个测试需要实际的网络连接
     */
    @Test
    public void testDoGet() {
        // 使用一个公共的测试API
        String url = "https://httpbin.org/get";
        String response = HttpUtils.doGet(url);
        
        // 验证响应不为空且包含预期内容
        assertNotNull(response);
        assertTrue(response.contains("httpbin.org"));
    }
    
    /**
     * 测试带参数的GET请求
     * 注意：这个测试需要实际的网络连接
     */
    @Test
    public void testDoGetWithParams() {
        // 使用一个公共的测试API
        String url = "https://httpbin.org/get";
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        
        String response = HttpUtils.doGet(url, params);
        
        // 验证响应不为空且包含预期内容
        assertNotNull(response);
        assertTrue(response.contains("param1"));
        assertTrue(response.contains("value1"));
        assertTrue(response.contains("param2"));
        assertTrue(response.contains("value2"));
    }
    
    /**
     * 测试POST请求
     * 注意：这个测试需要实际的网络连接
     */
    @Test
    public void testDoPost() {
        // 使用一个公共的测试API
        String url = "https://httpbin.org/post";
        String response = HttpUtils.doPost(url);
        
        // 验证响应不为空且包含预期内容
        assertNotNull(response);
        assertTrue(response.contains("httpbin.org"));
    }
    
    /**
     * 测试带表单参数的POST请求
     * 注意：这个测试需要实际的网络连接
     */
    @Test
    public void testDoPostWithParams() {
        // 使用一个公共的测试API
        String url = "https://httpbin.org/post";
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        
        String response = HttpUtils.doPost(url, params);
        
        // 验证响应不为空且包含预期内容
        assertNotNull(response);
        assertTrue(response.contains("param1"));
        assertTrue(response.contains("value1"));
        assertTrue(response.contains("param2"));
        assertTrue(response.contains("value2"));
    }
    
    /**
     * 测试带JSON数据的POST请求
     * 注意：这个测试需要实际的网络连接
     */
    @Test
    public void testDoPostJson() {
        // 使用一个公共的测试API
        String url = "https://httpbin.org/post";
        String jsonData = "{\"name\":\"John\",\"age\":30}";
        
        String response = HttpUtils.doPostJson(url, jsonData);
        
        // 验证响应不为空且包含预期内容
        assertNotNull(response);
        assertTrue(response.contains("John"));
        assertTrue(response.contains("30"));
    }
    
    /**
     * 测试关闭资源方法
     * 注意：由于closeResources是私有方法，无法直接测试
     * 这里通过测试其他方法间接验证其功能
     */
    @Test
    public void testCloseResources() {
        // 这个测试主要是确保其他测试方法能够正常执行
        // 如果closeResources方法有问题，其他测试方法可能会抛出异常
        testDoGet();
    }
}