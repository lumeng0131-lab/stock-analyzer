package com.stockanalyzer.service;

import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.dao.AnalysisResultDao;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 股票分析服务类
 * 负责分析股票数据并生成交易信号
 */
public class AnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);
    private final StockService stockService;
    private final AnalysisResultDao analysisResultDao;
    private final AppConfig appConfig;

    public AnalysisService() {
        this.stockService = new StockService();
        this.analysisResultDao = new AnalysisResultDao();
        this.appConfig = AppConfig.getInstance();
    }

    /**
     * 分析所有股票
     */
    public List<AnalysisResult> analyzeAllStocks() {
        List<AnalysisResult> results = new ArrayList<>();
        List<Stock> stocks = stockService.getAllStocks();
        
        for (Stock stock : stocks) {
            try {
                AnalysisResult result = analyzeStock(stock);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                logger.error("分析股票失败: {}", stock.getSymbol(), e);
            }
        }
        
        return results;
    }

    /**
     * 分析单个股票
     */
    public AnalysisResult analyzeStock(Stock stock) {
        logger.info("开始分析股票: {}", stock.getSymbol());
        
        // 获取股票价格数据
        List<StockPrice> prices = stockService.getStockPriceHistory(stock.getId());
        if (prices.isEmpty()) {
            logger.warn("没有足够的价格数据进行分析: {}", stock.getSymbol());
            return null;
        }
        
        // 创建时间序列
        TimeSeries series = createTimeSeries(prices);
        if (series.getBarCount() < 30) {
            logger.warn("没有足够的价格数据进行分析: {}", stock.getSymbol());
            return null;
        }
        
        // 执行多种分析
        AnalysisResult swingResult = analyzeSwingTrading(stock, series);
        AnalysisResult maResult = analyzeMovingAverages(stock, series);
        
        // 选择置信度最高的结果
        AnalysisResult finalResult = selectBestResult(swingResult, maResult);
        if (finalResult != null) {
            // 保存分析结果
            analysisResultDao.save(finalResult);
            logger.info("分析结果: {} {}", stock.getSymbol(), finalResult.getSignal());
        }
        
        return finalResult;
    }

    /**
     * 创建时间序列
     */
    private TimeSeries createTimeSeries(List<StockPrice> prices) {
        TimeSeries series = new BaseTimeSeries();
        
        for (StockPrice price : prices) {
            ZonedDateTime date = price.getDate().atStartOfDay(ZoneId.systemDefault());
            double open = price.getOpen().doubleValue();
            double high = price.getHigh().doubleValue();
            double low = price.getLow().doubleValue();
            double close = price.getClose().doubleValue();
            double volume = price.getVolume().doubleValue();
            
            series.addBar(date, open, high, low, close, volume);
        }
        
        return series;
    }

    /**
     * 波段交易分析
     */
    private AnalysisResult analyzeSwingTrading(Stock stock, TimeSeries series) {
        double threshold = appConfig.getSwingThreshold() / 100.0; // 转换为小数
        int shortTermDays = appConfig.getShortTermDays();
        
        // 获取最近的价格
        Bar latestBar = series.getLastBar();
        double latestClose = latestBar.getClosePrice().doubleValue();
        
        // 计算最近N天的价格范围
        double[] priceRange = calculatePriceRange(series, shortTermDays);
        double low = priceRange[0];
        double high = priceRange[1];
        
        // 计算当前价格在范围中的位置
        double range = high - low;
        if (range == 0) {
            return null; // 避免除以零
        }
        
        double position = (latestClose - low) / range;
        
        // 生成信号
        String signal;
        BigDecimal confidence;
        String description;
        
        if (position < 0.2) { // 接近底部
            signal = "BUY";
            confidence = BigDecimal.valueOf(80 + (0.2 - position) * 100).min(BigDecimal.valueOf(95));
            description = String.format(
                    "股票价格接近%d天内低点 %.2f，当前价格 %.2f，建议买入做多。",
                    shortTermDays, low, latestClose);
        } else if (position > 0.8) { // 接近顶部
            signal = "SELL";
            confidence = BigDecimal.valueOf(80 + (position - 0.8) * 100).min(BigDecimal.valueOf(95));
            description = String.format(
                    "股票价格接近%d天内高点 %.2f，当前价格 %.2f，建议卖出做空。",
                    shortTermDays, high, latestClose);
        } else { // 中间区域
            signal = "HOLD";
            confidence = BigDecimal.valueOf(50);
            description = String.format(
                    "股票价格处于%d天内波动区间 [%.2f, %.2f] 的中间位置，当前价格 %.2f，建议观望。",
                    shortTermDays, low, high, latestClose);
        }
        
        // 检查波动范围是否足够大
        double volatility = range / low;
        if (volatility < threshold) {
            signal = "HOLD";
            confidence = BigDecimal.valueOf(60);
            description = String.format(
                    "股票价格波动范围较小，%d天内波动仅为 %.2f%%，当前价格 %.2f，建议观望。",
                    shortTermDays, volatility * 100, latestClose);
        }
        
        // 创建分析结果
        AnalysisResult result = new AnalysisResult();
        result.setId(UUID.randomUUID().toString());
        result.setStockId(stock.getId());
        result.setAnalysisDate(LocalDate.now());
        result.setAnalysisType("SWING_TRADING");
        result.setSignal(signal);
        result.setConfidence(confidence);
        result.setDescription(description);
        result.setStock(stock);
        
        return result;
    }

    /**
     * 移动平均线分析
     */
    private AnalysisResult analyzeMovingAverages(Stock stock, TimeSeries series) {
        // 创建收盘价指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // 创建短期和长期移动平均线
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5); // 5日均线
        SMAIndicator longSma = new SMAIndicator(closePrice, 20); // 20日均线
        
        // 创建交易规则
        Rule buyRule = new CrossedUpIndicatorRule(shortSma, longSma); // 金叉
        Rule sellRule = new CrossedDownIndicatorRule(shortSma, longSma); // 死叉
        
        // 获取最新的指标值
        int lastIndex = series.getEndIndex();
        double lastClose = closePrice.getValue(lastIndex).doubleValue();
        double lastShortSma = shortSma.getValue(lastIndex).doubleValue();
        double lastLongSma = longSma.getValue(lastIndex).doubleValue();
        
        // 检查是否满足交易规则
        String signal;
        BigDecimal confidence;
        String description;
        
        if (buyRule.isSatisfied(lastIndex)) {
            signal = "BUY";
            confidence = BigDecimal.valueOf(85);
            description = String.format(
                    "短期均线(5日)上穿长期均线(20日)，形成金叉，当前价格 %.2f，5日均线 %.2f，20日均线 %.2f，建议买入。",
                    lastClose, lastShortSma, lastLongSma);
        } else if (sellRule.isSatisfied(lastIndex)) {
            signal = "SELL";
            confidence = BigDecimal.valueOf(85);
            description = String.format(
                    "短期均线(5日)下穿长期均线(20日)，形成死叉，当前价格 %.2f，5日均线 %.2f，20日均线 %.2f，建议卖出。",
                    lastClose, lastShortSma, lastLongSma);
        } else {
            // 检查价格相对于均线的位置
            if (lastClose > lastShortSma && lastShortSma > lastLongSma) {
                signal = "HOLD_BULLISH"; // 多头排列
                confidence = BigDecimal.valueOf(70);
                description = String.format(
                        "价格位于短期均线之上，且短期均线位于长期均线之上，呈多头排列，当前价格 %.2f，5日均线 %.2f，20日均线 %.2f，建议持有多单。",
                        lastClose, lastShortSma, lastLongSma);
            } else if (lastClose < lastShortSma && lastShortSma < lastLongSma) {
                signal = "HOLD_BEARISH"; // 空头排列
                confidence = BigDecimal.valueOf(70);
                description = String.format(
                        "价格位于短期均线之下，且短期均线位于长期均线之下，呈空头排列，当前价格 %.2f，5日均线 %.2f，20日均线 %.2f，建议持有空单。",
                        lastClose, lastShortSma, lastLongSma);
            } else {
                signal = "HOLD";
                confidence = BigDecimal.valueOf(60);
                description = String.format(
                        "均线关系不明确，当前价格 %.2f，5日均线 %.2f，20日均线 %.2f，建议观望。",
                        lastClose, lastShortSma, lastLongSma);
            }
        }
        
        // 创建分析结果
        AnalysisResult result = new AnalysisResult();
        result.setId(UUID.randomUUID().toString());
        result.setStockId(stock.getId());
        result.setAnalysisDate(LocalDate.now());
        result.setAnalysisType("MOVING_AVERAGE");
        result.setSignal(signal);
        result.setConfidence(confidence);
        result.setDescription(description);
        result.setStock(stock);
        
        return result;
    }

    /**
     * 计算价格范围
     */
    private double[] calculatePriceRange(TimeSeries series, int days) {
        int endIndex = series.getEndIndex();
        int startIndex = Math.max(0, endIndex - days + 1);
        
        double low = Double.MAX_VALUE;
        double high = Double.MIN_VALUE;
        
        for (int i = startIndex; i <= endIndex; i++) {
            Bar bar = series.getBar(i);
            double close = bar.getClosePrice().doubleValue();
            
            if (close < low) {
                low = close;
            }
            if (close > high) {
                high = close;
            }
        }
        
        return new double[]{low, high};
    }

    /**
     * 选择最佳分析结果
     */
    private AnalysisResult selectBestResult(AnalysisResult... results) {
        AnalysisResult bestResult = null;
        BigDecimal highestConfidence = BigDecimal.ZERO;
        
        for (AnalysisResult result : results) {
            if (result != null && result.getConfidence().compareTo(highestConfidence) > 0) {
                highestConfidence = result.getConfidence();
                bestResult = result;
            }
        }
        
        return bestResult;
    }

    /**
     * 获取最近的分析结果
     */
    public List<AnalysisResult> getRecentResults(int limit) {
        return analysisResultDao.findRecent(limit);
    }

    /**
     * 获取特定股票的分析结果
     */
    public List<AnalysisResult> getResultsByStock(String stockId) {
        return analysisResultDao.findByStockId(stockId);
    }

    /**
     * 获取特定日期的分析结果
     */
    public List<AnalysisResult> getResultsByDate(LocalDate date) {
        return analysisResultDao.findByDate(date);
    }

    /**
     * 获取特定信号的分析结果
     */
    public List<AnalysisResult> getResultsBySignal(String signal) {
        return analysisResultDao.findBySignal(signal);
    }

    /**
     * 根据ID获取分析结果
     */
    public Optional<AnalysisResult> getResultById(String id) {
        return analysisResultDao.findById(id);
    }
}