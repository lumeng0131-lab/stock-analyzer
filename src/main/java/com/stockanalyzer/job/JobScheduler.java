package com.stockanalyzer.job;

import com.stockanalyzer.scheduler.TaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务调度器
 * 作为TaskScheduler的包装器，保持与现有代码的兼容性
 */
public class JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
    private final TaskScheduler taskScheduler;

    public JobScheduler() {
        this.taskScheduler = new TaskScheduler();
        logger.info("任务调度器初始化完成");
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        logger.info("启动所有定时任务");
        taskScheduler.startAllTasks();
        taskScheduler.addPriceMonitoringTask();
        logger.info("所有定时任务已启动");
    }

    /**
     * 关闭调度器
     */
    public void shutdown() {
        logger.info("正在关闭任务调度器");
        taskScheduler.shutdown();
    }
}