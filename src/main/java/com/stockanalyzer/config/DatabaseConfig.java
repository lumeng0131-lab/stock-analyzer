package com.stockanalyzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库配置类
 * 负责创建数据库连接和初始化数据库表结构
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private DatabaseConfig() {
        AppConfig appConfig = AppConfig.getInstance();
        
        // 创建数据源
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(appConfig.getDbDriver());
        ds.setUrl(appConfig.getDbUrl());
        ds.setUsername(appConfig.getDbUsername());
        ds.setPassword(appConfig.getDbPassword());
        
        this.dataSource = ds;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        
        // 初始化数据库表
        initDatabase();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void initDatabase() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建股票表
            stmt.execute("CREATE TABLE IF NOT EXISTS stocks (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "symbol VARCHAR(20) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "exchange VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            
            // 创建股票价格历史表
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_prices (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "stock_id VARCHAR(50) NOT NULL, " +
                    "date DATE NOT NULL, " +
                    "open DECIMAL(10,2), " +
                    "high DECIMAL(10,2), " +
                    "low DECIMAL(10,2), " +
                    "close DECIMAL(10,2), " +
                    "volume BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (stock_id) REFERENCES stocks(id), " +
                    "UNIQUE (stock_id, date)" +
                    ");");
            
            // 创建分析结果表
            stmt.execute("CREATE TABLE IF NOT EXISTS analysis_results (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "stock_id VARCHAR(50) NOT NULL, " +
                    "analysis_date DATE NOT NULL, " +
                    "analysis_type VARCHAR(50) NOT NULL, " +
                    "signal VARCHAR(20) NOT NULL, " +
                    "confidence DECIMAL(5,2), " +
                    "description TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (stock_id) REFERENCES stocks(id)" +
                    ");");
            
            // 创建新闻表
            stmt.execute("CREATE TABLE IF NOT EXISTS stock_news (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "stock_id VARCHAR(50), " +
                    "title VARCHAR(255) NOT NULL, " +
                    "source VARCHAR(100), " +
                    "url VARCHAR(255), " +
                    "publish_date TIMESTAMP, " +
                    "summary TEXT, " +
                    "sentiment VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (stock_id) REFERENCES stocks(id)" +
                    ");");
            
            // 创建通知记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "stock_id VARCHAR(50) NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "notification_type VARCHAR(50) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "sent_at TIMESTAMP, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (stock_id) REFERENCES stocks(id)" +
                    ");");
            
            logger.info("数据库表初始化完成");
            
            // 初始化股票数据
            initStockData();
            
        } catch (SQLException e) {
            logger.error("初始化数据库失败", e);
            throw new RuntimeException("初始化数据库失败", e);
        }
    }

    private void initStockData() {
        // 从配置中获取股票列表并插入数据库
        AppConfig appConfig = AppConfig.getInstance();
        for (AppConfig.Stock stock : appConfig.getStockList()) {
            try {
                // 检查股票是否已存在
                int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM stocks WHERE symbol = ?",
                        Integer.class,
                        stock.getSymbol());
                
                if (count == 0) {
                    // 插入新股票
                    jdbcTemplate.update(
                            "INSERT INTO stocks (id, symbol, name, exchange) VALUES (?, ?, ?, ?)",
                            java.util.UUID.randomUUID().toString(),
                            stock.getSymbol(),
                            stock.getName(),
                            stock.getExchange());
                    logger.info("添加股票: {}", stock);
                }
            } catch (Exception e) {
                logger.error("初始化股票数据失败: {}", stock, e);
            }
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}