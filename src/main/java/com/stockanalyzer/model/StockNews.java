package com.stockanalyzer.model;

import java.time.LocalDateTime;

/**
 * 股票新闻实体类
 * 对应数据库中的stock_news表
 */
public class StockNews {
    private String id;
    private String stockId;
    private String title;
    private String source;
    private String url;
    private LocalDateTime publishDate;
    private String summary;
    private String sentiment; // POSITIVE, NEGATIVE, NEUTRAL
    private LocalDateTime createdAt;
    
    // 关联的股票信息，非数据库字段
    private Stock stock;

    public StockNews() {
    }

    public StockNews(String id, String stockId, String title, String source, String url,
                    LocalDateTime publishDate, String summary, String sentiment) {
        this.id = id;
        this.stockId = stockId;
        this.title = title;
        this.source = source;
        this.url = url;
        this.publishDate = publishDate;
        this.summary = summary;
        this.sentiment = sentiment;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
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
        return "StockNews{" +
                "title='" + title + '\'' +
                ", source='" + source + '\'' +
                ", publishDate=" + publishDate +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}