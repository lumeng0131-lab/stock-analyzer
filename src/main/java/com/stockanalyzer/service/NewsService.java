package com.stockanalyzer.service;

import com.stockanalyzer.dao.StockNewsDao;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新闻服务类
 * 负责获取和处理股票相关的新闻数据
 */
public class NewsService {
    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private final StockNewsDao stockNewsDao;
    private final StockApiService apiService;

    public NewsService() {
        this.stockNewsDao = new StockNewsDao();
        this.apiService = new StockApiService();
    }

    /**
     * 获取并保存特定股票的最新新闻
     */
    public List<StockNews> fetchAndSaveNews(Stock stock) {
        try {
            logger.info("获取股票{}的最新新闻", stock.getSymbol());
            List<StockNews> newsList = apiService.getStockNews(stock.getSymbol());
            
            if (newsList.isEmpty()) {
                logger.warn("未找到股票{}的新闻", stock.getSymbol());
                return Collections.emptyList();
            }
            
            // 设置股票ID并保存
            List<StockNews> savedNews = new ArrayList<>();
            for (StockNews news : newsList) {
                news.setStockId(stock.getId());
                news.setStock(stock);
                StockNews savedNewsItem = stockNewsDao.save(news);
                if (savedNewsItem != null) {
                    savedNews.add(savedNewsItem);
                }
            }
            
            logger.info("成功保存{}条{}的新闻", savedNews.size(), stock.getSymbol());
            return savedNews;
        } catch (Exception e) {
            logger.error("获取股票{}的新闻失败", stock.getSymbol(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取并保存所有股票的最新新闻
     */
    public Map<String, List<StockNews>> fetchAndSaveNewsForAllStocks(List<Stock> stocks) {
        return stocks.stream()
                .collect(Collectors.toMap(
                        Stock::getSymbol,
                        this::fetchAndSaveNews
                ));
    }

    /**
     * 获取特定股票的最新新闻
     */
    public List<StockNews> getLatestNews(String stockId, int limit) {
        return stockNewsDao.findLatestByStockId(stockId, limit);
    }

    /**
     * 获取特定日期范围内的新闻
     */
    public List<StockNews> getNewsByDateRange(String stockId, LocalDate startDate, LocalDate endDate) {
        return stockNewsDao.findByDateRange(stockId, startDate, endDate);
    }

    /**
     * 获取特定情感的新闻
     */
    public List<StockNews> getNewsBySentiment(String stockId, String sentiment) {
        return stockNewsDao.findBySentiment(stockId, sentiment);
    }

    /**
     * 获取包含特定关键词的新闻
     */
    public List<StockNews> getNewsByKeyword(String stockId, String keyword) {
        return stockNewsDao.findByKeyword(stockId, keyword);
    }

    /**
     * 分析新闻情感
     * 简单实现，实际项目中可以使用NLP库或API进行更准确的情感分析
     */
    public String analyzeNewsSentiment(StockNews news) {
        if (news.getTitle() == null && news.getSummary() == null) {
            return "NEUTRAL";
        }
        
        String text = (news.getTitle() + " " + news.getSummary()).toLowerCase();
        
        // 积极词汇
        List<String> positiveWords = List.of(
                "上涨", "增长", "盈利", "利润", "收益", "突破", "创新高", "超预期", 
                "强劲", "看好", "乐观", "提升", "扩张", "成功", "领先", "优势",
                "rise", "gain", "profit", "growth", "increase", "up", "high", "exceed",
                "strong", "positive", "optimistic", "success", "advantage", "lead"
        );
        
        // 消极词汇
        List<String> negativeWords = List.of(
                "下跌", "亏损", "跌破", "低于", "失败", "风险", "担忧", "问题",
                "困难", "挑战", "降低", "减少", "裁员", "危机", "调查", "处罚",
                "fall", "drop", "loss", "decline", "decrease", "down", "low", "below",
                "fail", "risk", "concern", "problem", "difficult", "challenge", "reduce", "cut"
        );
        
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (String word : positiveWords) {
            if (text.contains(word)) {
                positiveCount++;
            }
        }
        
        for (String word : negativeWords) {
            if (text.contains(word)) {
                negativeCount++;
            }
        }
        
        if (positiveCount > negativeCount && positiveCount > 0) {
            return "POSITIVE";
        } else if (negativeCount > positiveCount && negativeCount > 0) {
            return "NEGATIVE";
        } else {
            return "NEUTRAL";
        }
    }

    /**
     * 分析所有未分析情感的新闻
     */
    public void analyzeAllNewsSentiment() {
        List<StockNews> newsWithoutSentiment = stockNewsDao.findBySentiment(null, null);
        logger.info("发现{}条未分析情感的新闻", newsWithoutSentiment.size());
        
        for (StockNews news : newsWithoutSentiment) {
            String sentiment = analyzeNewsSentiment(news);
            news.setSentiment(sentiment);
            stockNewsDao.save(news);
        }
        
        logger.info("完成{}条新闻的情感分析", newsWithoutSentiment.size());
    }

    /**
     * 清理旧新闻
     * 删除指定日期之前的新闻
     */
    public int cleanupOldNews(LocalDate beforeDate) {
        int count = stockNewsDao.deleteBeforeDate(beforeDate);
        logger.info("已清理{}条{}之前的旧新闻", count, beforeDate);
        return count;
    }

    /**
     * 关闭服务
     */
    public void close() {
        apiService.close();
    }
}