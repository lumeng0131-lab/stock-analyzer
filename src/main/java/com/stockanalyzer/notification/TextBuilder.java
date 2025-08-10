package com.stockanalyzer.notification;

import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文本构建器
 * 负责构建各种通知的文本内容
 */
public class TextBuilder {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    /**
     * 构建分析结果通知的标题
     */
    public static String buildAnalysisTitle(AnalysisResult result) {
        Stock stock = result.getStock();
        return String.format("%s - %s信号 (置信度: %s%%)", 
                stock.getName(), result.getSignal(), result.getConfidence());
    }
    
    /**
     * 构建分析结果通知的内容
     */
    public static String buildAnalysisContent(AnalysisResult result) {
        Stock stock = result.getStock();
        return String.format(
                "## %s (%s) 交易信号\n\n" +
                "**信号类型**: %s\n\n" +
                "**置信度**: %s%%\n\n" +
                "**分析日期**: %s\n\n" +
                "**分析详情**:\n%s\n\n" +
                "---\n" +
                "*此消息由StockAnalyzer自动生成*",
                stock.getName(), stock.getSymbol(),
                result.getSignal(),
                result.getConfidence(),
                result.getAnalysisDate().format(DATE_FORMATTER),
                result.getDescription());
    }
    
    /**
     * 构建价格波动通知的标题
     */
    public static String buildPriceAlertTitle(Stock stock, double changePercent) {
        String direction = changePercent > 0 ? "上涨" : "下跌";
        return String.format("%s - 价格%s %.2f%%", stock.getName(), direction, Math.abs(changePercent));
    }
    
    /**
     * 构建价格波动通知的内容
     */
    public static String buildPriceAlertContent(Stock stock, double currentPrice, double changePercent) {
        return String.format(
                "## %s (%s) 价格波动提醒\n\n" +
                "**当前价格**: %.2f\n\n" +
                "**价格变动**: %s%.2f%%\n\n" +
                "**提醒时间**: %s\n\n" +
                "---\n" +
                "*此消息由StockAnalyzer自动生成*",
                stock.getName(), stock.getSymbol(),
                currentPrice,
                changePercent > 0 ? "+" : "",
                changePercent,
                LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }
    
    /**
     * 构建新闻通知的标题
     */
    public static String buildNewsTitle(Stock stock, int newsCount) {
        return String.format("%s - %d条最新相关新闻", stock.getName(), newsCount);
    }
    
    /**
     * 构建新闻通知的内容
     */
    public static String buildNewsContent(Stock stock, List<StockNews> newsList) {
        if (newsList.isEmpty()) {
            return "";
        }
        
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(String.format("## %s (%s) 相关新闻\n\n", stock.getName(), stock.getSymbol()));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int count = 0;
        for (StockNews news : newsList) {
            if (count++ >= 5) break; // 最多显示5条新闻
            
            String publishTime = news.getPublishDate() != null ? 
                    news.getPublishDate().format(formatter) : "未知时间";
            
            contentBuilder.append(String.format(
                    "### %s\n" +
                    "**来源**: %s | **时间**: %s | **情感**: %s\n\n" +
                    "%s\n\n" +
                    "[阅读全文](%s)\n\n",
                    news.getTitle(),
                    news.getSource(),
                    publishTime,
                    translateSentiment(news.getSentiment()),
                    news.getSummary(),
                    news.getUrl()));
        }
        
        contentBuilder.append("---\n*此消息由StockAnalyzer自动生成*");
        return contentBuilder.toString();
    }
    
    /**
     * 翻译情感分析结果为中文
     */
    private static String translateSentiment(String sentiment) {
        if (sentiment == null) {
            return "未知";
        }
        
        switch (sentiment.toUpperCase()) {
            case "POSITIVE":
                return "积极";
            case "NEGATIVE":
                return "消极";
            case "NEUTRAL":
                return "中性";
            default:
                return "未知";
        }
    }
}