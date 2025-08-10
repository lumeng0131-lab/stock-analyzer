package com.stockanalyzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 应用程序配置类
 * 单例模式，负责加载和管理应用程序的配置信息
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;
    private Properties properties;

    // 应用基本信息
    private String appName;
    private String appVersion;
    
    // 数据库配置
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    
    // API配置
    private String apiKey;
    private String apiBaseUrl;
    
    // 微信推送配置
    private String wechatKey;
    private String wechatUrl;
    
    // 定时任务配置
    private String stockFetchCron;
    private String stockAnalysisCron;
    
    // 股票列表
    private List<Stock> stockList;
    
    // 交易策略配置
    private double swingThreshold;
    private int shortTermDays;

    private AppConfig() {
        loadProperties();
        initConfig();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.error("无法找到配置文件: {}", CONFIG_FILE);
                throw new RuntimeException("无法找到配置文件: " + CONFIG_FILE);
            }
            properties.load(input);
            logger.debug("配置文件加载成功: {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
            throw new RuntimeException("加载配置文件失败", e);
        }
    }

    private void initConfig() {
        // 应用基本信息
        appName = properties.getProperty("app.name", "StockAnalyzer");
        appVersion = properties.getProperty("app.version", "1.0");
        
        // 数据库配置
        dbUrl = properties.getProperty("database.url");
        dbUsername = properties.getProperty("database.username");
        dbPassword = properties.getProperty("database.password");
        dbDriver = properties.getProperty("database.driver");
        
        // API配置
        apiKey = properties.getProperty("api.alphavantage.key");
        apiBaseUrl = properties.getProperty("api.alphavantage.baseUrl");
        
        // 微信推送配置
        wechatKey = properties.getProperty("wechat.serverChan.key");
        wechatUrl = properties.getProperty("wechat.serverChan.url");
        
        // 定时任务配置
        stockFetchCron = properties.getProperty("job.stockFetch.cron");
        stockAnalysisCron = properties.getProperty("job.stockAnalysis.cron");
        
        // 解析股票列表
        parseStockList();
        
        // 交易策略配置
        swingThreshold = Double.parseDouble(properties.getProperty("strategy.swing.threshold", "5"));
        shortTermDays = Integer.parseInt(properties.getProperty("strategy.shortTerm.days", "5"));
    }

    private void parseStockList() {
        stockList = new ArrayList<>();
        String stocksStr = properties.getProperty("stocks.list", "");
        if (!stocksStr.isEmpty()) {
            String[] stocks = stocksStr.split(";");
            for (String stock : stocks) {
                String[] parts = stock.split(",");
                if (parts.length >= 3) {
                    stockList.add(new Stock(parts[0], parts[1], parts[2]));
                }
            }
        }
        logger.info("加载了{}只股票信息", stockList.size());
    }

    // 内部股票类
    public static class Stock {
        private String symbol;
        private String name;
        private String exchange;

        public Stock(String symbol, String name, String exchange) {
            this.symbol = symbol;
            this.name = name;
            this.exchange = exchange;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getName() {
            return name;
        }

        public String getExchange() {
            return exchange;
        }
        
        @Override
        public String toString() {
            return name + "(" + symbol + ")" + "[" + exchange + "]";
        }
    }

    // Getters
    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public String getWechatKey() {
        return wechatKey;
    }

    public String getWechatUrl() {
        return wechatUrl;
    }

    public String getStockFetchCron() {
        return stockFetchCron;
    }

    public String getStockAnalysisCron() {
        return stockAnalysisCron;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public double getSwingThreshold() {
        return swingThreshold;
    }

    public int getShortTermDays() {
        return shortTermDays;
    }
}