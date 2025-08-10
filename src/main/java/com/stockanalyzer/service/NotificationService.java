package com.stockanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.dao.NotificationDao;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Notification;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;
import com.stockanalyzer.notification.TextBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 通知服务类
 * 负责发送微信推送通知
 */
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationDao notificationDao;
    private final AppConfig appConfig;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NotificationService() {
        this.notificationDao = new NotificationDao();
        this.appConfig = AppConfig.getInstance();
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 创建分析结果通知
     */
    public Notification createAnalysisNotification(AnalysisResult result) {
        Stock stock = result.getStock();
        if (stock == null) {
            logger.error("无法创建通知：分析结果缺少股票信息");
            return null;
        }
        
        String title = TextBuilder.buildAnalysisTitle(result);
        String content = TextBuilder.buildAnalysisContent(result);
        
        Notification notification = new Notification();
        notification.setStockId(stock.getId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType("ANALYSIS_RESULT");
        notification.setStatus("PENDING");
        notification.setStock(stock);
        
        return notificationDao.save(notification);
    }

    /**
     * 创建价格波动通知
     */
    public Notification createPriceAlertNotification(Stock stock, double currentPrice, double changePercent) {
        String title = TextBuilder.buildPriceAlertTitle(stock, changePercent);
        String content = TextBuilder.buildPriceAlertContent(stock, currentPrice, changePercent);
        
        Notification notification = new Notification();
        notification.setStockId(stock.getId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType("PRICE_ALERT");
        notification.setStatus("PENDING");
        notification.setStock(stock);
        
        return notificationDao.save(notification);
    }

    /**
     * 创建新闻通知
     */
    public Notification createNewsNotification(Stock stock, List<StockNews> newsList) {
        if (newsList.isEmpty()) {
            return null;
        }
        
        String title = TextBuilder.buildNewsTitle(stock, newsList.size());
        String content = TextBuilder.buildNewsContent(stock, newsList);
        
        Notification notification = new Notification();
        notification.setStockId(stock.getId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotificationType("NEWS_ALERT");
        notification.setStatus("PENDING");
        notification.setStock(stock);
        
        return notificationDao.save(notification);
    }

    /**
     * 发送所有待发送的通知
     */
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications = notificationDao.findPendingNotifications();
        logger.info("发现{}条待发送的通知", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                boolean success = sendNotification(notification);
                if (success) {
                    notificationDao.markAsSent(notification.getId());
                    logger.info("通知发送成功: {}", notification.getTitle());
                } else {
                    notificationDao.markAsFailed(notification.getId());
                    logger.error("通知发送失败: {}", notification.getTitle());
                }
                
                // 避免频繁发送
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("发送通知异常: {}", notification.getTitle(), e);
                notificationDao.markAsFailed(notification.getId());
            }
        }
    }

    /**
     * 发送单个通知
     */
    private boolean sendNotification(Notification notification) throws IOException, URISyntaxException {
        String serverChanKey = appConfig.getWechatKey();
        String serverChanUrl = appConfig.getWechatUrl();
        
        if (serverChanKey == null || serverChanKey.isEmpty() || 
            serverChanKey.equalsIgnoreCase("YOUR_SENDKEY")) {
            logger.warn("未配置Server酱Turbo版SendKey，无法发送通知");
            return false;
        }
        
        // 构建请求URL
        URI uri = new URIBuilder(serverChanUrl + serverChanKey + ".send")
                .build();
        
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setHeader("Content-Type", "application/json");
        
        // 构建请求体
        String requestBody = objectMapper.writeValueAsString(new ServerChanRequest(
                notification.getTitle(),
                notification.getContent()
        ));
        
        httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        
        // 发送请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                JsonNode resultNode = objectMapper.readTree(result);
                
                // 检查响应
                int code = resultNode.path("code").asInt(-1);
                return code == 0; // 0表示成功
            }
        }
        
        return false;
    }



    /**
     * 获取最近的通知
     */
    public List<Notification> getRecentNotifications(int limit) {
        return notificationDao.findRecent(limit);
    }

    /**
     * 获取特定股票的通知
     */
    public List<Notification> getNotificationsByStock(String stockId) {
        return notificationDao.findByStockId(stockId);
    }

    /**
     * 关闭服务
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("关闭HTTP客户端失败", e);
        }
    }

    /**
     * Server酱请求体
     */
    private static class ServerChanRequest {
        private final String title;
        private final String desp;

        public ServerChanRequest(String title, String desp) {
            this.title = title;
            this.desp = desp;
        }

        public String getTitle() {
            return title;
        }

        public String getDesp() {
            return desp;
        }
    }
}