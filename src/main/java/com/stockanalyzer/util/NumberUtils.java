package com.stockanalyzer.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 数值工具类
 * 提供数值计算、格式化和转换等常用功能
 */
public class NumberUtils {
    
    /**
     * 默认的小数位数
     */
    private static final int DEFAULT_SCALE = 2;
    
    /**
     * 默认的舍入模式
     */
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * 将double值四舍五入到指定小数位数
     * 
     * @param value 原始值
     * @param scale 小数位数
     * @return 四舍五入后的值
     */
    public static double round(double value, int scale) {
        return round(value, scale, DEFAULT_ROUNDING_MODE);
    }
    
    /**
     * 将double值按指定舍入模式舍入到指定小数位数
     * 
     * @param value 原始值
     * @param scale 小数位数
     * @param roundingMode 舍入模式
     * @return 舍入后的值
     */
    public static double round(double value, int scale, RoundingMode roundingMode) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(scale, roundingMode);
        return bd.doubleValue();
    }
    
    /**
     * 将double值四舍五入到2位小数
     * 
     * @param value 原始值
     * @return 四舍五入后的值
     */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }
    
    /**
     * 格式化double值为字符串，保留指定小数位数
     * 
     * @param value 原始值
     * @param scale 小数位数
     * @return 格式化后的字符串
     */
    public static String format(double value, int scale) {
        StringBuilder pattern = new StringBuilder("0.");
        for (int i = 0; i < scale; i++) {
            pattern.append("0");
        }
        DecimalFormat df = new DecimalFormat(pattern.toString());
        return df.format(value);
    }
    
    /**
     * 格式化double值为字符串，保留2位小数
     * 
     * @param value 原始值
     * @return 格式化后的字符串
     */
    public static String format(double value) {
        return format(value, DEFAULT_SCALE);
    }
    
    /**
     * 计算两个数的百分比变化
     * 计算公式：(newValue - oldValue) / oldValue * 100
     * 
     * @param oldValue 旧值
     * @param newValue 新值
     * @return 百分比变化，以百分比表示（如5.25表示5.25%）
     */
    public static double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return 0;
        }
        double change = (newValue - oldValue) / Math.abs(oldValue) * 100;
        return round(change, DEFAULT_SCALE);
    }
    
    /**
     * 计算平均值
     * 
     * @param values 数值数组
     * @return 平均值
     */
    public static double average(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return round(sum / values.length, DEFAULT_SCALE);
    }
    
    /**
     * 计算标准差
     * 
     * @param values 数值数组
     * @return 标准差
     */
    public static double standardDeviation(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double avg = average(values);
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - avg, 2);
        }
        return round(Math.sqrt(sum / values.length), DEFAULT_SCALE);
    }
    
    /**
     * 计算方差
     * 
     * @param values 数值数组
     * @return 方差
     */
    public static double variance(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double avg = average(values);
        double sum = 0;
        for (double value : values) {
            sum += Math.pow(value - avg, 2);
        }
        return round(sum / values.length, DEFAULT_SCALE);
    }
    
    /**
     * 计算最大值
     * 
     * @param values 数值数组
     * @return 最大值
     */
    public static double max(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }
    
    /**
     * 计算最小值
     * 
     * @param values 数值数组
     * @return 最小值
     */
    public static double min(double[] values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }
    
    /**
     * 安全的字符串转整数，如果转换失败返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 转换后的整数
     */
    public static int toInt(String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 安全的字符串转浮点数，如果转换失败返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 转换后的浮点数
     */
    public static double toDouble(String str, double defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}