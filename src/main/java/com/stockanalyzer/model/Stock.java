package com.stockanalyzer.model;

import java.time.LocalDateTime;

/**
 * 股票实体类
 * 对应数据库中的stocks表
 */
public class Stock {
    private String id;
    private String symbol;
    private String name;
    private String exchange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stock() {
    }

    public Stock(String id, String symbol, String name, String exchange) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return name + "(" + symbol + ")" + "[" + exchange + "]";
    }
}