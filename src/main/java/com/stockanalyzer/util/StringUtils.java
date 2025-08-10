package com.stockanalyzer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 提供字符串处理的常用功能
 */
public class StringUtils {
    
    /**
     * 判断字符串是否为空或null
     * 
     * @param str 字符串
     * @return 如果字符串为null或空字符串，返回true；否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * 判断字符串是否不为空且不为null
     * 
     * @param str 字符串
     * @return 如果字符串不为null且不为空字符串，返回true；否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否为空白字符串
     * 空白字符串包括：null、空字符串、只包含空格的字符串
     * 
     * @param str 字符串
     * @return 如果字符串为null、空字符串或只包含空格，返回true；否则返回false
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空白字符串
     * 
     * @param str 字符串
     * @return 如果字符串不为null、不为空字符串且不只包含空格，返回true；否则返回false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 截取字符串，如果字符串为null，返回null
     * 
     * @param str 字符串
     * @param start 开始位置（包含）
     * @param end 结束位置（不包含）
     * @return 截取后的字符串
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        
        // 调整参数
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return "";
        }
        
        return str.substring(start, end);
    }
    
    /**
     * 将字符串数组连接成一个字符串
     * 
     * @param array 字符串数组
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(String[] array, String separator) {
        if (array == null || array.length == 0) {
            return "";
        }
        if (separator == null) {
            separator = "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            if (array[i] != null) {
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * 将List连接成一个字符串
     * 
     * @param list 字符串列表
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(List<String> list, String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return join(list.toArray(new String[0]), separator);
    }
    
    /**
     * 将字符串按照指定分隔符分割成字符串数组
     * 
     * @param str 字符串
     * @param separator 分隔符
     * @return 分割后的字符串数组
     */
    public static String[] split(String str, String separator) {
        if (isEmpty(str)) {
            return new String[0];
        }
        if (separator == null) {
            return new String[] {str};
        }
        return str.split(separator);
    }
    
    /**
     * 从字符串中提取所有匹配正则表达式的子串
     * 
     * @param str 字符串
     * @param regex 正则表达式
     * @return 匹配的子串列表
     */
    public static List<String> extractAll(String str, String regex) {
        List<String> result = new ArrayList<>();
        if (isEmpty(str) || isEmpty(regex)) {
            return result;
        }
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
    
    /**
     * 判断字符串是否匹配正则表达式
     * 
     * @param str 字符串
     * @param regex 正则表达式
     * @return 如果匹配返回true，否则返回false
     */
    public static boolean matches(String str, String regex) {
        if (str == null || regex == null) {
            return false;
        }
        return str.matches(regex);
    }
    
    /**
     * 将字符串的首字母转为大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    /**
     * 将字符串的首字母转为小写
     * 
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}