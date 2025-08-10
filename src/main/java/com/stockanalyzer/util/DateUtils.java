package com.stockanalyzer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 * 提供日期格式化、解析和计算等常用功能
 */
public class DateUtils {
    
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 将日期格式化为字符串，使用默认格式 yyyy-MM-dd
     * 
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 将日期格式化为字符串，使用指定格式
     * 
     * @param date 日期
     * @param pattern 日期格式
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 将字符串解析为日期，使用默认格式 yyyy-MM-dd
     * 
     * @param dateStr 日期字符串
     * @return 解析后的日期
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr) throws ParseException {
        return parseDate(dateStr, DEFAULT_DATE_FORMAT);
    }
    
    /**
     * 将字符串解析为日期，使用指定格式
     * 
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 解析后的日期
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr, String pattern) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(dateStr);
    }
    
    /**
     * 获取当前日期
     * 
     * @return 当前日期
     */
    public static Date getCurrentDate() {
        return new Date();
    }
    
    /**
     * 获取当前日期字符串，使用默认格式 yyyy-MM-dd
     * 
     * @return 当前日期字符串
     */
    public static String getCurrentDateStr() {
        return formatDate(getCurrentDate());
    }
    
    /**
     * 获取当前日期时间字符串，使用默认格式 yyyy-MM-dd HH:mm:ss
     * 
     * @return 当前日期时间字符串
     */
    public static String getCurrentDateTimeStr() {
        return formatDate(getCurrentDate(), DEFAULT_DATETIME_FORMAT);
    }
    
    /**
     * 将Date转换为LocalDate
     * 
     * @param date Date对象
     * @return LocalDate对象
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    /**
     * 将LocalDate转换为Date
     * 
     * @param localDate LocalDate对象
     * @return Date对象
     */
    public static Date fromLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 计算两个日期之间的天数差
     * 
     * @param date1 日期1
     * @param date2 日期2
     * @return 天数差
     */
    public static long daysBetween(Date date1, Date date2) {
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);
        return Math.abs(localDate1.toEpochDay() - localDate2.toEpochDay());
    }
    
    /**
     * 获取指定天数前的日期
     * 
     * @param date 基准日期
     * @param days 天数
     * @return 指定天数前的日期
     */
    public static Date minusDays(Date date, long days) {
        LocalDate localDate = toLocalDate(date);
        return fromLocalDate(localDate.minusDays(days));
    }
    
    /**
     * 获取指定天数后的日期
     * 
     * @param date 基准日期
     * @param days 天数
     * @return 指定天数后的日期
     */
    public static Date plusDays(Date date, long days) {
        LocalDate localDate = toLocalDate(date);
        return fromLocalDate(localDate.plusDays(days));
    }
}