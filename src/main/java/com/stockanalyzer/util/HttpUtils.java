package com.stockanalyzer.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP工具类
 * 提供HTTP请求的常用功能
 */
public class HttpUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    
    // 默认超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5000;
    
    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @return 响应内容
     */
    public static String doGet(String url) {
        return doGet(url, null);
    }
    
    /**
     * 发送带参数的GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @return 响应内容
     */
    public static String doGet(String url, Map<String, String> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String result = null;
        
        try {
            // 创建URI并添加参数
            URIBuilder builder = new URIBuilder(url);
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();
            
            // 创建GET请求
            HttpGet httpGet = new HttpGet(uri);
            
            // 设置请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                    .build();
            httpGet.setConfig(requestConfig);
            
            // 执行请求
            response = httpClient.execute(httpGet);
            
            // 处理响应
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (response != null) {
                logger.error("HTTP GET request failed with status code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (URISyntaxException | IOException e) {
            logger.error("Error during HTTP GET request to {}: {}", url, e.getMessage());
        } finally {
            // 关闭资源
            closeResources(httpClient, response);
        }
        
        return result;
    }
    
    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @return 响应内容
     */
    public static String doPost(String url) {
        return doPost(url, null);
    }
    
    /**
     * 发送带表单参数的POST请求
     * 
     * @param url 请求URL
     * @param params 表单参数
     * @return 响应内容
     */
    public static String doPost(String url, Map<String, String> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String result = null;
        
        try {
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                    .build();
            httpPost.setConfig(requestConfig);
            
            // 设置表单参数
            if (params != null) {
                List<NameValuePair> formParams = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
            }
            
            // 执行请求
            response = httpClient.execute(httpPost);
            
            // 处理响应
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (response != null) {
                logger.error("HTTP POST request failed with status code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            logger.error("Error during HTTP POST request to {}: {}", url, e.getMessage());
        } finally {
            // 关闭资源
            closeResources(httpClient, response);
        }
        
        return result;
    }
    
    /**
     * 发送带JSON数据的POST请求
     * 
     * @param url 请求URL
     * @param jsonData JSON数据
     * @return 响应内容
     */
    public static String doPostJson(String url, String jsonData) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String result = null;
        
        try {
            // 创建POST请求
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                    .build();
            httpPost.setConfig(requestConfig);
            
            // 设置JSON数据
            if (jsonData != null) {
                StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            
            // 执行请求
            response = httpClient.execute(httpPost);
            
            // 处理响应
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (response != null) {
                logger.error("HTTP POST JSON request failed with status code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            logger.error("Error during HTTP POST JSON request to {}: {}", url, e.getMessage());
        } finally {
            // 关闭资源
            closeResources(httpClient, response);
        }
        
        return result;
    }
    
    /**
     * 关闭HTTP客户端和响应资源
     * 
     * @param httpClient HTTP客户端
     * @param response HTTP响应
     */
    private static void closeResources(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HTTP resources: {}", e.getMessage());
        }
    }
    
    /**
     * 获取响应状态码
     * 
     * @param response HTTP响应
     * @return 状态码
     */
    public static int getStatusCode(HttpResponse response) {
        if (response == null) {
            return -1;
        }
        return response.getStatusLine().getStatusCode();
    }
}