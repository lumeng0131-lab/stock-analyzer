package com.stockanalyzer.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分析结果实体类
 * 对应数据库中的analysis_results表
 */
public class AnalysisResult {
    private String id;
    private String stockId;
    private LocalDate analysisDate;
    private String analysisType;
    private String signal; // BUY, SELL, HOLD
    private BigDecimal confidence; // 0-100的置信度
    private String description;
    private LocalDateTime createdAt;
    
    // 关联的股票信息，非数据库字段
    private Stock stock;

    public AnalysisResult() {
    }

    public AnalysisResult(String id, String stockId, LocalDate analysisDate, String analysisType,
                         String signal, BigDecimal confidence, String description) {
        this.id = id;
        this.stockId = stockId;
        this.analysisDate = analysisDate;
        this.analysisType = analysisType;
        this.signal = signal;
        this.confidence = confidence;
        this.description = description;
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

    public LocalDate getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDate analysisDate) {
        this.analysisDate = analysisDate;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return "AnalysisResult{" +
                "analysisDate=" + analysisDate +
                ", analysisType='" + analysisType + '\'' +
                ", signal='" + signal + '\'' +
                ", confidence=" + confidence +
                ", description='" + description + '\'' +
                '}';
    }
}