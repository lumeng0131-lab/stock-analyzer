package com.stockanalyzer.cli;

import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Notification;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockPrice;
import com.stockanalyzer.scheduler.TaskScheduler;
import com.stockanalyzer.service.AnalysisService;
import com.stockanalyzer.service.NewsService;
import com.stockanalyzer.service.NotificationService;
import com.stockanalyzer.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行界面
 * 提供用户交互和手动执行任务的功能
 */
public class CommandLineInterface {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineInterface.class);
    private final Scanner scanner;
    private final StockService stockService;
    private final AnalysisService analysisService;
    private final NewsService newsService;
    private final NotificationService notificationService;
    private final TaskScheduler taskScheduler;
    private final AppConfig appConfig;
    private boolean running;

    public CommandLineInterface() {
        this.scanner = new Scanner(System.in);
        this.stockService = new StockService();
        this.analysisService = new AnalysisService();
        this.newsService = new NewsService();
        this.notificationService = new NotificationService();
        this.taskScheduler = new TaskScheduler();
        this.appConfig = AppConfig.getInstance();
        this.running = true;
    }

    /**
     * 启动命令行界面
     */
    public void start() {
        printWelcome();
        
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            try {
                processChoice(choice);
            } catch (Exception e) {
                logger.error("处理命令时发生错误", e);
                System.out.println("操作失败: " + e.getMessage());
            }
            
            System.out.println();
        }
        
        scanner.close();
    }

    /**
     * 打印欢迎信息
     */
    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("  欢迎使用股票分析器 v" + appConfig.getVersion());
        System.out.println("  作者: " + appConfig.getAuthor());
        System.out.println("========================================");
        System.out.println();
    }

    /**
     * 打印菜单
     */
    private void printMenu() {
        System.out.println("请选择操作:");
        System.out.println("1. 查看所有股票");
        System.out.println("2. 添加新股票");
        System.out.println("3. 获取股票历史数据");
        System.out.println("4. 分析股票");
        System.out.println("5. 查看最近分析结果");
        System.out.println("6. 获取股票新闻");
        System.out.println("7. 发送待发送通知");
        System.out.println("8. 查看最近通知");
        System.out.println("9. 启动所有定时任务");
        System.out.println("0. 退出");
        System.out.print("请输入选项: ");
    }

    /**
     * 处理用户选择
     */
    private void processChoice(String choice) {
        switch (choice) {
            case "1" -> listAllStocks();
            case "2" -> addNewStock();
            case "3" -> fetchStockData();
            case "4" -> analyzeStock();
            case "5" -> viewRecentAnalysisResults();
            case "6" -> fetchStockNews();
            case "7" -> sendPendingNotifications();
            case "8" -> viewRecentNotifications();
            case "9" -> startScheduledTasks();
            case "0" -> exit();
            default -> System.out.println("无效选项，请重新输入");
        }
    }

    /**
     * 列出所有股票
     */
    private void listAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        
        if (stocks.isEmpty()) {
            System.out.println("未找到任何股票，请先添加股票");
            return;
        }
        
        System.out.println("\n当前所有股票:");
        System.out.println("ID\t代码\t\t名称\t\t交易所");
        System.out.println("----------------------------------------");
        
        for (Stock stock : stocks) {
            System.out.printf("%s\t%s\t%s\t%s\n",
                    stock.getId(),
                    stock.getSymbol(),
                    padRight(stock.getName(), 8),
                    stock.getExchange());
        }
    }

    /**
     * 添加新股票
     */
    private void addNewStock() {
        System.out.print("请输入股票代码: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("请输入股票名称: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("请输入交易所 (例如: NASDAQ, NYSE, SSE, SZSE): ");
        String exchange = scanner.nextLine().trim().toUpperCase();
        
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setName(name);
        stock.setExchange(exchange);
        
        Stock savedStock = stockService.saveStock(stock);
        System.out.println("股票添加成功: " + savedStock.getSymbol() + " - " + savedStock.getName());
    }

    /**
     * 获取股票历史数据
     */
    private void fetchStockData() {
        System.out.print("请输入股票ID或代码 (输入'all'获取所有股票数据): ");
        String input = scanner.nextLine().trim();
        
        if ("all".equalsIgnoreCase(input)) {
            List<Stock> stocks = stockService.getAllStocks();
            System.out.println("开始获取所有股票的历史数据...");
            
            for (Stock stock : stocks) {
                try {
                    System.out.println("正在获取 " + stock.getSymbol() + " 的数据...");
                    List<StockPrice> prices = stockService.fetchAndUpdateStockData(stock.getId());
                    System.out.println("成功获取 " + prices.size() + " 条价格数据");
                    Thread.sleep(1000); // 避免API限流
                } catch (Exception e) {
                    System.out.println("获取 " + stock.getSymbol() + " 数据失败: " + e.getMessage());
                }
            }
            
            System.out.println("所有股票数据获取完成");
        } else {
            Stock stock;
            if (input.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                stock = stockService.getStockById(input);
            } else {
                stock = stockService.getStockBySymbol(input);
            }
            
            if (stock == null) {
                System.out.println("未找到股票: " + input);
                return;
            }
            
            System.out.println("正在获取 " + stock.getSymbol() + " 的数据...");
            List<StockPrice> prices = stockService.fetchAndUpdateStockData(stock.getId());
            System.out.println("成功获取 " + prices.size() + " 条价格数据");
            
            // 显示最近的价格数据
            if (!prices.isEmpty()) {
                System.out.println("\n最近的价格数据:");
                System.out.println("日期\t\t开盘价\t最高价\t最低价\t收盘价\t成交量");
                System.out.println("------------------------------------------------------------");
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (int i = 0; i < Math.min(5, prices.size()); i++) {
                    StockPrice price = prices.get(i);
                    System.out.printf("%s\t%.2f\t%.2f\t%.2f\t%.2f\t%d\n",
                            price.getDate().format(formatter),
                            price.getOpen(),
                            price.getHigh(),
                            price.getLow(),
                            price.getClose(),
                            price.getVolume());
                }
            }
        }
    }

    /**
     * 分析股票
     */
    private void analyzeStock() {
        System.out.print("请输入股票ID或代码 (输入'all'分析所有股票): ");
        String input = scanner.nextLine().trim();
        
        if ("all".equalsIgnoreCase(input)) {
            System.out.println("开始分析所有股票...");
            List<AnalysisResult> results = analysisService.analyzeAllStocks();
            System.out.println("分析完成，共生成 " + results.size() + " 个分析结果");
            
            // 显示分析结果
            if (!results.isEmpty()) {
                displayAnalysisResults(results);
            }
        } else {
            Stock stock;
            if (input.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                stock = stockService.getStockById(input);
            } else {
                stock = stockService.getStockBySymbol(input);
            }
            
            if (stock == null) {
                System.out.println("未找到股票: " + input);
                return;
            }
            
            System.out.println("正在分析 " + stock.getSymbol() + " ...");
            AnalysisResult result = analysisService.analyzeStock(stock.getId());
            
            if (result != null) {
                System.out.println("\n分析结果:");
                System.out.println("股票: " + result.getStock().getSymbol() + " - " + result.getStock().getName());
                System.out.println("分析日期: " + result.getAnalysisDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                System.out.println("分析类型: " + result.getAnalysisType());
                System.out.println("交易信号: " + result.getSignal());
                System.out.println("置信度: " + result.getConfidence() + "%");
                System.out.println("描述: " + result.getDescription());
            } else {
                System.out.println("分析失败，可能是数据不足");
            }
        }
    }

    /**
     * 显示分析结果列表
     */
    private void displayAnalysisResults(List<AnalysisResult> results) {
        System.out.println("\n分析结果:");
        System.out.println("股票代码\t信号\t置信度\t分析日期\t分析类型");
        System.out.println("------------------------------------------------------------");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (AnalysisResult result : results) {
            System.out.printf("%s\t%s\t%.1f%%\t%s\t%s\n",
                    result.getStock().getSymbol(),
                    padRight(result.getSignal(), 6),
                    result.getConfidence(),
                    result.getAnalysisDate().format(formatter),
                    result.getAnalysisType());
        }
    }

    /**
     * 查看最近分析结果
     */
    private void viewRecentAnalysisResults() {
        System.out.print("请输入要查看的结果数量 (默认10): ");
        String input = scanner.nextLine().trim();
        
        int limit = 10;
        if (!input.isEmpty()) {
            try {
                limit = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("无效输入，使用默认值10");
            }
        }
        
        List<AnalysisResult> results = analysisService.getRecentResults(limit);
        
        if (results.isEmpty()) {
            System.out.println("未找到任何分析结果");
            return;
        }
        
        displayAnalysisResults(results);
    }

    /**
     * 获取股票新闻
     */
    private void fetchStockNews() {
        System.out.print("请输入股票ID或代码: ");
        String input = scanner.nextLine().trim();
        
        Stock stock;
        if (input.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            stock = stockService.getStockById(input);
        } else {
            stock = stockService.getStockBySymbol(input);
        }
        
        if (stock == null) {
            System.out.println("未找到股票: " + input);
            return;
        }
        
        System.out.println("正在获取 " + stock.getSymbol() + " 的新闻...");
        newsService.fetchAndSaveNews(stock);
        
        // 获取并显示最新新闻
        System.out.print("请输入要显示的新闻数量 (默认5): ");
        input = scanner.nextLine().trim();
        
        int limit = 5;
        if (!input.isEmpty()) {
            try {
                limit = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("无效输入，使用默认值5");
            }
        }
        
        var newsList = newsService.getLatestNews(stock.getId(), limit);
        
        if (newsList.isEmpty()) {
            System.out.println("未找到任何新闻");
            return;
        }
        
        System.out.println("\n" + stock.getSymbol() + " 的最新新闻:");
        System.out.println("------------------------------------------------------------");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (var news : newsList) {
            System.out.println("标题: " + news.getTitle());
            System.out.println("来源: " + news.getSource() + 
                    " | 时间: " + (news.getPublishDate() != null ? news.getPublishDate().format(formatter) : "未知") + 
                    " | 情感: " + translateSentiment(news.getSentiment()));
            System.out.println("摘要: " + news.getSummary());
            System.out.println("链接: " + news.getUrl());
            System.out.println("------------------------------------------------------------");
        }
    }

    /**
     * 翻译情感分析结果
     */
    private String translateSentiment(String sentiment) {
        if (sentiment == null) {
            return "中性";
        }
        
        return switch (sentiment.toUpperCase()) {
            case "POSITIVE" -> "积极";
            case "NEGATIVE" -> "消极";
            default -> "中性";
        };
    }

    /**
     * 发送待发送通知
     */
    private void sendPendingNotifications() {
        System.out.println("正在发送待发送的通知...");
        notificationService.sendPendingNotifications();
        System.out.println("通知发送完成");
    }

    /**
     * 查看最近通知
     */
    private void viewRecentNotifications() {
        System.out.print("请输入要查看的通知数量 (默认10): ");
        String input = scanner.nextLine().trim();
        
        int limit = 10;
        if (!input.isEmpty()) {
            try {
                limit = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("无效输入，使用默认值10");
            }
        }
        
        List<Notification> notifications = notificationService.getRecentNotifications(limit);
        
        if (notifications.isEmpty()) {
            System.out.println("未找到任何通知");
            return;
        }
        
        System.out.println("\n最近的通知:");
        System.out.println("ID\t股票\t类型\t\t状态\t标题");
        System.out.println("------------------------------------------------------------");
        
        for (Notification notification : notifications) {
            System.out.printf("%s\t%s\t%s\t%s\t%s\n",
                    notification.getId().substring(0, 8),
                    notification.getStock().getSymbol(),
                    padRight(notification.getNotificationType(), 12),
                    padRight(notification.getStatus(), 6),
                    notification.getTitle());
        }
    }

    /**
     * 启动所有定时任务
     */
    private void startScheduledTasks() {
        System.out.println("正在启动所有定时任务...");
        taskScheduler.startAllTasks();
        taskScheduler.addPriceMonitoringTask();
        System.out.println("所有定时任务已启动");
    }

    /**
     * 退出程序
     */
    private void exit() {
        System.out.println("正在退出程序...");
        running = false;
    }

    /**
     * 字符串右侧填充空格
     */
    private String padRight(String s, int n) {
        if (s == null) {
            s = "";
        }
        return String.format("%-" + n + "s", s);
    }
}