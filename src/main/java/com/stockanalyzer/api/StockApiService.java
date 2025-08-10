package com.stockanalyzer.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;
import com.stockanalyzer.model.StockPrice;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 股票API服务
 * 负责从Alpha Vantage API获取股票数据
 */
public class StockApiService {
    private static final Logger logger = LoggerFactory.getLogger(StockApiService.class);
    private final String apiKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public StockApiService() {
        AppConfig appConfig = AppConfig.getInstance();
        this.apiKey = appConfig.getApiKey();
        this.baseUrl = appConfig.getApiBaseUrl();
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 获取股票日线数据
     */
    public List<StockPrice> getDailyPrices(Stock stock) throws IOException, URISyntaxException {
        logger.info("获取股票日线数据: {}", stock.getSymbol());
        
        URI uri = new URIBuilder(baseUrl)
                .addParameter("function", "TIME_SERIES_DAILY")
                .addParameter("symbol", stock.getSymbol())
                .addParameter("outputsize", "compact") // compact返回最近100个交易日数据，full返回全部数据
                .addParameter("apikey", apiKey)
                .build();
        
        HttpGet request = new HttpGet(uri);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return parseDailyPrices(result, stock.getId());
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * 解析日线数据响应
     */
    private List<StockPrice> parseDailyPrices(String jsonResponse, String stockId) throws IOException {
        List<StockPrice> prices = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        // 检查是否有错误信息
        if (rootNode.has("Error Message")) {
            logger.error("API错误: {}", rootNode.get("Error Message").asText());
            return prices;
        }
        
        // 获取时间序列数据
        JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");
        if (timeSeriesNode == null) {
            logger.error("API响应中没有时间序列数据");
            return prices;
        }
        
        // 遍历每一天的数据
        Iterator<String> dateFields = timeSeriesNode.fieldNames();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (dateFields.hasNext()) {
            String dateStr = dateFields.next();
            JsonNode priceNode = timeSeriesNode.get(dateStr);
            
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                
                StockPrice price = new StockPrice();
                price.setStockId(stockId);
                price.setDate(date);
                price.setOpen(new BigDecimal(priceNode.get("1. open").asText()));
                price.setHigh(new BigDecimal(priceNode.get("2. high").asText()));
                price.setLow(new BigDecimal(priceNode.get("3. low").asText()));
                price.setClose(new BigDecimal(priceNode.get("4. close").asText()));
                price.setVolume(Long.parseLong(priceNode.get("5. volume").asText()));
                
                prices.add(price);
            } catch (DateTimeParseException e) {
                logger.error("日期解析错误: {}", dateStr, e);
            } catch (NumberFormatException e) {
                logger.error("数字解析错误: {}", dateStr, e);
            }
        }
        
        logger.info("解析到{}条股票价格数据", prices.size());
        return prices;
    }

    /**
     * 获取股票新闻数据
     */
    public List<StockNews> getStockNews(Stock stock) throws IOException, URISyntaxException {
        logger.info("获取股票新闻: {}", stock.getSymbol());
        
        URI uri = new URIBuilder(baseUrl)
                .addParameter("function", "NEWS_SENTIMENT")
                .addParameter("tickers", stock.getSymbol())
                .addParameter("limit", "10") // 限制返回10条新闻
                .addParameter("apikey", apiKey)
                .build();
        
        HttpGet request = new HttpGet(uri);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return parseStockNews(result, stock.getId());
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * 解析新闻数据响应
     */
    private List<StockNews> parseStockNews(String jsonResponse, String stockId) throws IOException {
        List<StockNews> newsList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        // 检查是否有错误信息
        if (rootNode.has("Error Message")) {
            logger.error("API错误: {}", rootNode.get("Error Message").asText());
            return newsList;
        }
        
        // 获取新闻数据
        JsonNode feedNode = rootNode.get("feed");
        if (feedNode == null || !feedNode.isArray()) {
            logger.error("API响应中没有新闻数据");
            return newsList;
        }
        
        // 遍历每条新闻
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        
        for (JsonNode newsNode : feedNode) {
            try {
                StockNews news = new StockNews();
                news.setStockId(stockId);
                news.setTitle(newsNode.get("title").asText());
                news.setSource(newsNode.get("source").asText());
                news.setUrl(newsNode.get("url").asText());
                
                // 解析发布时间
                String timeStr = newsNode.get("time_published").asText();
                news.setPublishDate(LocalDateTime.parse(timeStr, formatter));
                
                // 获取摘要
                news.setSummary(newsNode.get("summary").asText());
                
                // 获取情感分析
                if (newsNode.has("overall_sentiment_label")) {
                    news.setSentiment(newsNode.get("overall_sentiment_label").asText().toUpperCase());
                } else {
                    news.setSentiment("NEUTRAL");
                }
                
                newsList.add(news);
            } catch (Exception e) {
                logger.error("解析新闻数据错误", e);
            }
        }
        
        logger.info("解析到{}条新闻数据", newsList.size());
        return newsList;
    }

    /**
     * 获取股票概览信息
     */
    public JsonNode getStockOverview(String symbol) throws IOException, URISyntaxException {
        logger.info("获取股票概览: {}", symbol);
        
        URI uri = new URIBuilder(baseUrl)
                .addParameter("function", "OVERVIEW")
                .addParameter("symbol", symbol)
                .addParameter("apikey", apiKey)
                .build();
        
        HttpGet request = new HttpGet(uri);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return objectMapper.readTree(result);
            }
        }
        
        return objectMapper.createObjectNode();
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("关闭HTTP客户端失败", e);
        }
    }
}