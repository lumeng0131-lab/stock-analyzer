package com.stockanalyzer.scheduler;

import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Notification;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;
import com.stockanalyzer.service.AnalysisService;
import com.stockanalyzer.service.NewsService;
import com.stockanalyzer.service.NotificationService;
import com.stockanalyzer.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度器
 * 负责定时执行股票数据抓取、分析和通知发送任务
 */
public class TaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TaskScheduler.class);
    private final ScheduledExecutorService scheduler;
    private final StockService stockService;
    private final AnalysisService analysisService;
    private final NewsService newsService;
    private final NotificationService notificationService;
    private final AppConfig appConfig;

    public TaskScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.stockService = new StockService();
        this.analysisService = new AnalysisService();
        this.newsService = new NewsService();
        this.notificationService = new NotificationService();
        this.appConfig = AppConfig.getInstance();
    }

    /**
     * 启动所有定时任务
     */
    public void startAllTasks() {
        logger.info("启动所有定时任务");
        
        // 数据抓取任务 - 每个交易日的收盘后执行
        scheduleDataFetchTask();
        
        // 数据分析任务 - 每个交易日的数据抓取完成后执行
        scheduleAnalysisTask();
        
        // 新闻抓取任务 - 每天多次执行
        scheduleNewsFetchTask();
        
        // 通知发送任务 - 定期检查并发送待发送的通知
        scheduleNotificationTask();
        
        // 立即执行一次初始化数据抓取
        scheduler.schedule(this::fetchInitialData, 5, TimeUnit.SECONDS);
    }

    /**
     * 初始化数据抓取
     */
    private void fetchInitialData() {
        try {
            logger.info("执行初始化数据抓取");
            List<Stock> stocks = stockService.getAllStocks();
            
            if (stocks.isEmpty()) {
                logger.warn("未找到任何股票数据，请确保数据库中已初始化股票信息");
                return;
            }
            
            // 抓取历史数据
            for (Stock stock : stocks) {
                try {
                    logger.info("抓取股票{}的历史数据", stock.getSymbol());
                    stockService.fetchAndUpdateStockData(stock.getId());
                    // 避免API限流
                    Thread.sleep(1000);
                } catch (Exception e) {
                    logger.error("抓取股票{}的历史数据失败", stock.getSymbol(), e);
                }
            }
            
            // 抓取新闻数据
            newsService.fetchAndSaveNewsForAllStocks(stocks);
            
            // 执行初始分析
            analysisService.analyzeAllStocks();
            
            logger.info("初始化数据抓取完成");
        } catch (Exception e) {
            logger.error("初始化数据抓取失败", e);
        }
    }

    /**
     * 调度数据抓取任务
     * 每个交易日（周一至周五）的收盘后执行
     */
    private void scheduleDataFetchTask() {
        String fetchTimeStr = appConfig.getDataFetchTime();
        LocalTime fetchTime = LocalTime.parse(fetchTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                DayOfWeek today = now.getDayOfWeek();
                
                // 只在工作日执行
                if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
                    logger.info("执行每日数据抓取任务");
                    List<Stock> stocks = stockService.getAllStocks();
                    
                    for (Stock stock : stocks) {
                        try {
                            stockService.fetchAndUpdateStockData(stock.getId());
                            // 避免API限流
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            logger.error("抓取股票{}数据失败", stock.getSymbol(), e);
                        }
                    }
                    
                    logger.info("每日数据抓取任务完成");
                }
            } catch (Exception e) {
                logger.error("执行数据抓取任务时发生错误", e);
            }
        }, getInitialDelay(fetchTime), 24, TimeUnit.HOURS);
        
        logger.info("数据抓取任务已调度，将在每个工作日{}执行", fetchTimeStr);
    }

    /**
     * 调度数据分析任务
     * 在数据抓取完成后执行
     */
    private void scheduleAnalysisTask() {
        String analysisTimeStr = appConfig.getAnalysisTime();
        LocalTime analysisTime = LocalTime.parse(analysisTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                DayOfWeek today = now.getDayOfWeek();
                
                // 只在工作日执行
                if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY) {
                    logger.info("执行每日数据分析任务");
                    
                    // 分析所有股票
                    List<AnalysisResult> results = analysisService.analyzeAllStocks();
                    
                    // 为分析结果创建通知
                    for (AnalysisResult result : results) {
                        // 只为买入或卖出信号创建通知
                        if ("BUY".equals(result.getSignal()) || "SELL".equals(result.getSignal())) {
                            notificationService.createAnalysisNotification(result);
                        }
                    }
                    
                    logger.info("每日数据分析任务完成，生成{}个分析结果", results.size());
                }
            } catch (Exception e) {
                logger.error("执行数据分析任务时发生错误", e);
            }
        }, getInitialDelay(analysisTime), 24, TimeUnit.HOURS);
        
        logger.info("数据分析任务已调度，将在每个工作日{}执行", analysisTimeStr);
    }

    /**
     * 调度新闻抓取任务
     * 每天多次执行
     */
    private void scheduleNewsFetchTask() {
        int newsIntervalHours = appConfig.getNewsIntervalHours();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("执行新闻抓取任务");
                List<Stock> stocks = stockService.getAllStocks();
                
                Map<String, List<StockNews>> allNews = newsService.fetchAndSaveNewsForAllStocks(stocks);
                
                // 分析新闻情感
                newsService.analyzeAllNewsSentiment();
                
                // 为重要新闻创建通知
                for (Stock stock : stocks) {
                    List<StockNews> stockNews = allNews.get(stock.getSymbol());
                    if (stockNews != null && !stockNews.isEmpty()) {
                        // 筛选出积极或消极的新闻
                        List<StockNews> importantNews = stockNews.stream()
                                .filter(news -> "POSITIVE".equals(news.getSentiment()) || 
                                               "NEGATIVE".equals(news.getSentiment()))
                                .toList();
                        
                        if (!importantNews.isEmpty()) {
                            notificationService.createNewsNotification(stock, importantNews);
                        }
                    }
                }
                
                // 清理旧新闻
                LocalDate cutoffDate = LocalDate.now().minusDays(30);
                newsService.cleanupOldNews(cutoffDate);
                
                logger.info("新闻抓取任务完成");
            } catch (Exception e) {
                logger.error("执行新闻抓取任务时发生错误", e);
            }
        }, 10, newsIntervalHours, TimeUnit.HOURS);
        
        logger.info("新闻抓取任务已调度，将每{}小时执行一次", newsIntervalHours);
    }

    /**
     * 调度通知发送任务
     * 定期检查并发送待发送的通知
     */
    private void scheduleNotificationTask() {
        int notificationIntervalMinutes = appConfig.getNotificationIntervalMinutes();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("执行通知发送任务");
                notificationService.sendPendingNotifications();
                logger.info("通知发送任务完成");
            } catch (Exception e) {
                logger.error("执行通知发送任务时发生错误", e);
            }
        }, 1, notificationIntervalMinutes, TimeUnit.MINUTES);
        
        logger.info("通知发送任务已调度，将每{}分钟执行一次", notificationIntervalMinutes);
    }

    /**
     * 计算初始延迟时间
     */
    private long getInitialDelay(LocalTime targetTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.with(targetTime);
        
        if (now.toLocalTime().isAfter(targetTime)) {
            nextRun = nextRun.plusDays(1);
        }
        
        return ChronoUnit.SECONDS.between(now, nextRun);
    }

    /**
     * 添加价格波动监控任务
     * 监控股票价格波动，当波动超过阈值时发送通知
     */
    public void addPriceMonitoringTask() {
        double priceAlertThreshold = appConfig.getPriceAlertThreshold();
        int monitoringIntervalMinutes = appConfig.getPriceMonitoringIntervalMinutes();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                DayOfWeek today = now.getDayOfWeek();
                LocalTime currentTime = now.toLocalTime();
                
                // 只在交易时间内执行
                if (today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY &&
                    isWithinTradingHours(currentTime)) {
                    
                    logger.info("执行价格监控任务");
                    List<Stock> stocks = stockService.getAllStocks();
                    
                    for (Stock stock : stocks) {
                        try {
                            // 获取最新价格
                            double currentPrice = stockService.getLatestPrice(stock.getId());
                            
                            // 获取昨日收盘价
                            double previousClose = stockService.getPreviousClosePrice(stock.getId());
                            
                            if (previousClose > 0) {
                                // 计算价格变动百分比
                                double changePercent = (currentPrice - previousClose) / previousClose * 100;
                                
                                // 如果价格变动超过阈值，创建通知
                                if (Math.abs(changePercent) >= priceAlertThreshold) {
                                    notificationService.createPriceAlertNotification(
                                            stock, currentPrice, changePercent);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("监控股票{}价格失败", stock.getSymbol(), e);
                        }
                    }
                    
                    logger.info("价格监控任务完成");
                }
            } catch (Exception e) {
                logger.error("执行价格监控任务时发生错误", e);
            }
        }, 2, monitoringIntervalMinutes, TimeUnit.MINUTES);
        
        logger.info("价格监控任务已调度，将在交易时间内每{}分钟执行一次，价格波动阈值为{}%", 
                monitoringIntervalMinutes, priceAlertThreshold);
    }

    /**
     * 判断当前时间是否在交易时间内
     */
    private boolean isWithinTradingHours(LocalTime time) {
        // 假设交易时间为9:30-11:30, 13:00-15:00
        return (time.isAfter(LocalTime.of(9, 30)) && time.isBefore(LocalTime.of(11, 30))) ||
               (time.isAfter(LocalTime.of(13, 0)) && time.isBefore(LocalTime.of(15, 0)));
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        try {
            logger.info("正在关闭任务调度器...");
            scheduler.shutdown();
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.warn("任务调度器未能在60秒内完全关闭，将强制关闭");
                scheduler.shutdownNow();
            }
            
            // 关闭服务
            stockService.close();
            newsService.close();
            notificationService.close();
            
            logger.info("任务调度器已关闭");
        } catch (InterruptedException e) {
            logger.error("关闭任务调度器时被中断", e);
            Thread.currentThread().interrupt();
        }
    }
}