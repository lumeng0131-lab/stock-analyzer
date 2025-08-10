package com.stockanalyzer.model;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库中的notifications表
 */
public class Notification {
    private String id;
    private String stockId;
    private String title;
    private String content;
    private String notificationType; // PRICE_ALERT, NEWS_ALERT, ANALYSIS_RESULT
    private String status; // PENDING, SENT, FAILED
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    
    // 关联的股票信息，非数据库字段
    private Stock stock;

    public Notification() {
    }

    public Notification(String id, String stockId, String title, String content, 
                       String notificationType, String status) {
        this.id = id;
        this.stockId = stockId;
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}