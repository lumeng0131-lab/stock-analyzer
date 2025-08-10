package com.stockanalyzer.util;

import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * DateUtils工具类的单元测试
 */
public class DateUtilsTest {

    @Test
    public void testFormatDate() {
        Date date = new Date(1609459200000L); // 2021-01-01 00:00:00
        String formatted = DateUtils.formatDate(date);
        assertEquals("2021-01-01", formatted);
        
        String customFormatted = DateUtils.formatDate(date, "yyyy/MM/dd");
        assertEquals("2021/01/01", customFormatted);
        
        assertNull(DateUtils.formatDate(null));
    }
    
    @Test
    public void testParseDate() throws ParseException {
        Date date = DateUtils.parseDate("2021-01-01");
        assertNotNull(date);
        assertEquals("2021-01-01", DateUtils.formatDate(date));
        
        Date customDate = DateUtils.parseDate("2021/01/01", "yyyy/MM/dd");
        assertNotNull(customDate);
        assertEquals("2021-01-01", DateUtils.formatDate(customDate));
        
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
    }
    
    @Test
    public void testGetCurrentDate() {
        Date currentDate = DateUtils.getCurrentDate();
        assertNotNull(currentDate);
        assertTrue(currentDate.getTime() <= System.currentTimeMillis());
    }
    
    @Test
    public void testGetCurrentDateStr() {
        String currentDateStr = DateUtils.getCurrentDateStr();
        assertNotNull(currentDateStr);
        assertTrue(currentDateStr.matches("\\d{4}-\\d{2}-\\d{2}"));
    }
    
    @Test
    public void testGetCurrentDateTimeStr() {
        String currentDateTimeStr = DateUtils.getCurrentDateTimeStr();
        assertNotNull(currentDateTimeStr);
        assertTrue(currentDateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
    
    @Test
    public void testToLocalDate() {
        Date date = new Date(1609459200000L); // 2021-01-01 00:00:00
        LocalDate localDate = DateUtils.toLocalDate(date);
        assertNotNull(localDate);
        assertEquals(2021, localDate.getYear());
        assertEquals(1, localDate.getMonthValue());
        assertEquals(1, localDate.getDayOfMonth());
        
        assertNull(DateUtils.toLocalDate(null));
    }
    
    @Test
    public void testFromLocalDate() {
        LocalDate localDate = LocalDate.of(2021, 1, 1);
        Date date = DateUtils.fromLocalDate(localDate);
        assertNotNull(date);
        assertEquals("2021-01-01", DateUtils.formatDate(date));
        
        assertNull(DateUtils.fromLocalDate(null));
    }
    
    @Test
    public void testDaysBetween() throws ParseException {
        Date date1 = DateUtils.parseDate("2021-01-01");
        Date date2 = DateUtils.parseDate("2021-01-10");
        long days = DateUtils.daysBetween(date1, date2);
        assertEquals(9, days);
        
        // 测试顺序无关性
        long daysReversed = DateUtils.daysBetween(date2, date1);
        assertEquals(9, daysReversed);
    }
    
    @Test
    public void testMinusDays() throws ParseException {
        Date date = DateUtils.parseDate("2021-01-10");
        Date result = DateUtils.minusDays(date, 5);
        assertEquals("2021-01-05", DateUtils.formatDate(result));
    }
    
    @Test
    public void testPlusDays() throws ParseException {
        Date date = DateUtils.parseDate("2021-01-10");
        Date result = DateUtils.plusDays(date, 5);
        assertEquals("2021-01-15", DateUtils.formatDate(result));
    }
}