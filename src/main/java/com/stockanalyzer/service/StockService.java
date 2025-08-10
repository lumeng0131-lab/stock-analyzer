package com.stockanalyzer.service;

import com.stockanalyzer.api.StockApiService;
import com.stockanalyzer.dao.StockDao;
import com.stockanalyzer.dao.StockPriceDao;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 股票服务类
 * 负责处理股票数据的业务逻辑
 */
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockDao stockDao;
    private final StockPriceDao stockPriceDao;
    private final StockApiService apiService;

    public StockService() {
        this.stockDao = new StockDao();
        this.stockPriceDao = new StockPriceDao();
        this.apiService = new StockApiService();
    }

    /**
     * 获取所有股票
     */
    public List<Stock> getAllStocks() {
        return stockDao.findAll();
    }

    /**
     * 根据ID获取股票
     */
    public Optional<Stock> getStockById(String id) {
        return stockDao.findById(id);
    }

    /**
     * 根据代码获取股票
     */
    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockDao.findBySymbol(symbol);
    }

    /**
     * 保存股票
     */
    public Stock saveStock(Stock stock) {
        return stockDao.save(stock);
    }

    /**
     * 删除股票
     */
    public boolean deleteStock(String id) {
        return stockDao.delete(id);
    }

    /**
     * 获取股票价格历史
     */
    public List<StockPrice> getStockPriceHistory(String stockId) {
        return stockPriceDao.findByStockId(stockId);
    }

    /**
     * 获取股票在指定日期范围内的价格历史
     */
    public List<StockPrice> getStockPriceHistory(String stockId, LocalDate startDate, LocalDate endDate) {
        return stockPriceDao.findByStockIdAndDateRange(stockId, startDate, endDate);
    }

    /**
     * 获取股票最近N天的价格历史
     */
    public List<StockPrice> getRecentStockPrices(String stockId, int days) {
        return stockPriceDao.findRecentPrices(stockId, days);
    }

    /**
     * 保存股票价格
     */
    public StockPrice saveStockPrice(StockPrice stockPrice) {
        return stockPriceDao.save(stockPrice);
    }

    /**
     * 批量保存股票价格
     */
    public void saveStockPrices(List<StockPrice> stockPrices) {
        stockPriceDao.saveAll(stockPrices);
    }

    /**
     * 从API获取并更新股票价格数据
     */
    public List<StockPrice> fetchAndUpdateStockPrices(Stock stock) {
        try {
            logger.info("从API获取股票价格数据: {}", stock.getSymbol());
            List<StockPrice> prices = apiService.getDailyPrices(stock);
            
            if (!prices.isEmpty()) {
                saveStockPrices(prices);
                logger.info("成功更新{}条股票价格数据: {}", prices.size(), stock.getSymbol());
            } else {
                logger.warn("未获取到股票价格数据: {}", stock.getSymbol());
            }
            
            return prices;
        } catch (Exception e) {
            logger.error("获取股票价格数据失败: {}", stock.getSymbol(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取股票的最新价格
     */
    public Optional<StockPrice> getLatestPrice(String stockId) {
        List<StockPrice> recentPrices = stockPriceDao.findRecentPrices(stockId, 1);
        if (!recentPrices.isEmpty()) {
            return Optional.of(recentPrices.get(0));
        }
        return Optional.empty();
    }

    /**
     * 计算股票的波动范围
     * @return 返回一个包含最低价和最高价的数组 [低, 高]
     */
    public double[] calculatePriceRange(String stockId, int days) {
        List<StockPrice> prices = getRecentStockPrices(stockId, days);
        if (prices.isEmpty()) {
            return new double[]{0, 0};
        }
        
        double low = Double.MAX_VALUE;
        double high = Double.MIN_VALUE;
        
        for (StockPrice price : prices) {
            double closePrice = price.getClose().doubleValue();
            if (closePrice < low) {
                low = closePrice;
            }
            if (closePrice > high) {
                high = closePrice;
            }
        }
        
        return new double[]{low, high};
    }

    /**
     * 计算股票的平均价格
     */
    public double calculateAveragePrice(String stockId, int days) {
        List<StockPrice> prices = getRecentStockPrices(stockId, days);
        if (prices.isEmpty()) {
            return 0;
        }
        
        double sum = 0;
        for (StockPrice price : prices) {
            sum += price.getClose().doubleValue();
        }
        
        return sum / prices.size();
    }

    /**
     * 计算股票的波动率
     */
    public double calculateVolatility(String stockId, int days) {
        List<StockPrice> prices = getRecentStockPrices(stockId, days);
        if (prices.size() < 2) {
            return 0;
        }
        
        // 计算每日收益率
        List<Double> returns = new ArrayList<>();
        for (int i = 0; i < prices.size() - 1; i++) {
            double today = prices.get(i).getClose().doubleValue();
            double yesterday = prices.get(i + 1).getClose().doubleValue();
            double dailyReturn = (today / yesterday) - 1;
            returns.add(dailyReturn);
        }
        
        // 计算收益率的标准差
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - mean, 2))
                .average()
                .orElse(0);
        
        // 年化波动率 (假设一年有252个交易日)
        return Math.sqrt(variance) * Math.sqrt(252);
    }

    /**
     * 关闭服务
     */
    public void close() {
        apiService.close();
    }
}