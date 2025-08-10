package com.stockanalyzer.job;

import com.stockanalyzer.scheduler.TaskScheduler;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * JobScheduler类的单元测试
 */
public class JobSchedulerTest {

    private JobScheduler jobScheduler;
    private TaskScheduler mockTaskScheduler;

    @Before
    public void setUp() throws Exception {
        // 创建模拟对象
        mockTaskScheduler = mock(TaskScheduler.class);
        
        // 创建JobScheduler实例
        jobScheduler = new JobScheduler();
        
        // 使用反射注入模拟对象
        Field field = JobScheduler.class.getDeclaredField("taskScheduler");
        field.setAccessible(true);
        field.set(jobScheduler, mockTaskScheduler);
    }

    /**
     * 测试启动所有任务
     */
    @Test
    public void testStartJobs() {
        // 执行测试方法
        jobScheduler.startJobs();
        
        // 验证TaskScheduler的startAllTasks方法被调用
        verify(mockTaskScheduler, times(1)).startAllTasks();
        
        // 验证TaskScheduler的addPriceMonitoringTask方法被调用
        verify(mockTaskScheduler, times(1)).addPriceMonitoringTask();
    }

    /**
     * 测试关闭调度器
     */
    @Test
    public void testShutdown() {
        // 执行测试方法
        jobScheduler.shutdown();
        
        // 验证TaskScheduler的shutdown方法被调用
        verify(mockTaskScheduler, times(1)).shutdown();
    }
}