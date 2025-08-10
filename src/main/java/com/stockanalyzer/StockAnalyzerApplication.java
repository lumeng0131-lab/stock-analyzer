package com.stockanalyzer;

import com.stockanalyzer.cli.CommandLineInterface;
import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.job.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 股票分析应用程序的主类
 * 负责初始化应用程序配置、数据库连接和定时任务
 */
public class StockAnalyzerApplication {
    private static final Logger logger = LoggerFactory.getLogger(StockAnalyzerApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("启动股票分析应用程序...");
            
            // 初始化应用配置
            AppConfig appConfig = AppConfig.getInstance();
            logger.info("应用配置加载完成: {} v{}", appConfig.getAppName(), appConfig.getAppVersion());
            
            // 初始化数据库
            initDatabase();
            
            // 启动定时任务
            JobScheduler scheduler = new JobScheduler();
            scheduler.startJobs();
            
            logger.info("应用程序启动成功，等待定时任务执行...");
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("应用程序正在关闭...");
                scheduler.shutdown();
                logger.info("应用程序已关闭");
            }));
            
            // 启动命令行界面
            CommandLineInterface cli = new CommandLineInterface();
            cli.start();
            
        } catch (Exception e) {
            logger.error("应用程序启动失败", e);
            System.exit(1);
        }
    }
    
    private static void initDatabase() {
        try {
            // 初始化数据库连接和表结构
            logger.info("初始化数据库...");
            // 这里会调用数据库初始化类，在后续实现
            logger.info("数据库初始化完成");
        } catch (Exception e) {
            logger.error("数据库初始化失败", e);
            throw e;
        }
    }
}