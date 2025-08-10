package com.stockanalyzer.scheduler;

import com.stockanalyzer.config.AppConfig;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Stock;
import com.stockanalyzer.model.StockNews;
import com.stockanalyzer.service.AnalysisService;
import com.stockanalyzer.service.NewsService;
import com.stockanalyzer.service.NotificationService;
import com.stockanalyzer.service.StockService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * TaskScheduler类的单元测试
 */
public class TaskSchedulerTest {

    private TaskScheduler taskScheduler;
    private StockService mockStockService;
    private AnalysisService mockAnalysisService;
    private NewsService mockNewsService;
    private NotificationService mockNotificationService;
    private ScheduledExecutorService mockScheduler;
    private AppConfig mockAppConfig;

    @Before
    public void setUp() throws Exception {
        // 创建模拟对象
        mockStockService = mock(StockService.class);
        mockAnalysisService = mock(AnalysisService.class);
        mockNewsService = mock(NewsService.class);
        mockNotificationService = mock(NotificationService.class);
        mockScheduler = mock(ScheduledExecutorService.class);
        mockAppConfig = mock(AppConfig.class);
        
        // 设置AppConfig的模拟行为
        when(mockAppConfig.getDataFetchTime()).thenReturn("16:00");
        when(mockAppConfig.getAnalysisTime()).thenReturn("16:30");
        when(mockAppConfig.getNewsIntervalHours()).thenReturn(4);
        when(mockAppConfig.getNotificationIntervalMinutes()).thenReturn(15);
        when(mockAppConfig.getPriceAlertThreshold()).thenReturn(5.0);
        when(mockAppConfig.getPriceMonitoringIntervalMinutes()).thenReturn(10);
        
        // 创建TaskScheduler实例
        taskScheduler = new TaskScheduler();
        
        // 使用反射注入模拟对象
        injectMocks();
    }

    @After
    public void tearDown() {
        taskScheduler.shutdown();
    }

    /**
     * 使用反射注入模拟对象
     */
    private void injectMocks() throws Exception {
        // 注入模拟的服务
        injectField("stockService", mockStockService);
        injectField("analysisService", mockAnalysisService);
        injectField("newsService", mockNewsService);
        injectField("notificationService", mockNotificationService);
        injectField("scheduler", mockScheduler);
        injectField("appConfig", mockAppConfig);
    }

    /**
     * 注入字段值
     */
    private void injectField(String fieldName, Object value) throws Exception {
        Field field = TaskScheduler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(taskScheduler, value);
    }

    /**
     * 测试启动所有任务
     */
    @Test
    public void testStartAllTasks() {
        // 执行测试方法
        taskScheduler.startAllTasks();
        
        // 验证调度器的scheduleAtFixedRate方法被调用了4次（数据抓取、数据分析、新闻抓取、通知发送）
        verify(mockScheduler, times(4)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
        
        // 验证调度器的schedule方法被调用了1次（初始化数据抓取）
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), anyLong(), any());
    }

    /**
     * 测试添加价格监控任务
     */
    @Test
    public void testAddPriceMonitoringTask() {
        // 执行测试方法
        taskScheduler.addPriceMonitoringTask();
        
        // 验证调度器的scheduleAtFixedRate方法被调用
        verify(mockScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
    }

    /**
     * 测试关闭调度器
     */
    @Test
    public void testShutdown() throws Exception {
        // 执行测试方法
        taskScheduler.shutdown();
        
        // 验证调度器的shutdown方法被调用
        verify(mockScheduler, times(1)).shutdown();
        
        // 验证服务的close方法被调用
        verify(mockStockService, times(1)).close();
        verify(mockNewsService, times(1)).close();
        verify(mockNotificationService, times(1)).close();
    }

    /**
     * 测试初始化数据抓取
     */
    @Test
    public void testFetchInitialData() throws Exception {
        // 准备测试数据
        List<Stock> stocks = new ArrayList<>();
        Stock stock1 = new Stock();
        stock1.setId(1L);
        stock1.setSymbol("AAPL");
        stocks.add(stock1);
        
        // 设置模拟对象的行为
        when(mockStockService.getAllStocks()).thenReturn(stocks);
        
        // 使用反射调用私有方法
        Method method = TaskScheduler.class.getDeclaredMethod("fetchInitialData");
        method.setAccessible(true);
        method.invoke(taskScheduler);
        
        // 验证方法调用
        verify(mockStockService, times(1)).getAllStocks();
        verify(mockStockService, times(1)).fetchAndUpdateStockData(1L);
        verify(mockNewsService, times(1)).fetchAndSaveNewsForAllStocks(stocks);
        verify(mockAnalysisService, times(1)).analyzeAllStocks();
    }

    /**
     * 测试判断交易时间
     */
    @Test
    public void testIsWithinTradingHours() throws Exception {
        // 使用反射调用私有方法
        Method method = TaskScheduler.class.getDeclaredMethod("isWithinTradingHours", LocalTime.class);
        method.setAccessible(true);
        
        // 测试交易时间内
        assertTrue((Boolean) method.invoke(taskScheduler, LocalTime.of(10, 0)));
        assertTrue((Boolean) method.invoke(taskScheduler, LocalTime.of(14, 0)));
        
        // 测试交易时间外
        assertFalse((Boolean) method.invoke(taskScheduler, LocalTime.of(8, 0)));
        assertFalse((Boolean) method.invoke(taskScheduler, LocalTime.of(12, 0)));
        assertFalse((Boolean) method.invoke(taskScheduler, LocalTime.of(16, 0)));
    }
}