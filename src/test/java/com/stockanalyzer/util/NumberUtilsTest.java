package com.stockanalyzer.util;

import org.junit.Test;

import java.math.RoundingMode;

import static org.junit.Assert.*;

/**
 * NumberUtils工具类的单元测试
 */
public class NumberUtilsTest {

    @Test
    public void testRound() {
        assertEquals(3.14, NumberUtils.round(3.14159, 2), 0.0001);
        assertEquals(3.142, NumberUtils.round(3.14159, 3), 0.0001);
        assertEquals(3.0, NumberUtils.round(3.14159, 0), 0.0001);
        assertEquals(3.15, NumberUtils.round(3.14501, 2), 0.0001);
        assertEquals(3.14, NumberUtils.round(3.14159), 0.0001);
    }
    
    @Test
    public void testRoundWithRoundingMode() {
        assertEquals(3.14, NumberUtils.round(3.14159, 2, RoundingMode.HALF_UP), 0.0001);
        assertEquals(3.14, NumberUtils.round(3.14159, 2, RoundingMode.HALF_DOWN), 0.0001);
        assertEquals(3.14, NumberUtils.round(3.14159, 2, RoundingMode.DOWN), 0.0001);
        assertEquals(3.15, NumberUtils.round(3.14501, 2, RoundingMode.HALF_UP), 0.0001);
        assertEquals(3.14, NumberUtils.round(3.14501, 2, RoundingMode.DOWN), 0.0001);
    }
    
    @Test
    public void testFormat() {
        assertEquals("3.14", NumberUtils.format(3.14159));
        assertEquals("3.142", NumberUtils.format(3.14159, 3));
        assertEquals("3", NumberUtils.format(3.14159, 0));
        assertEquals("3.15", NumberUtils.format(3.14501));
    }
    
    @Test
    public void testCalculatePercentageChange() {
        assertEquals(5.0, NumberUtils.calculatePercentageChange(100, 105), 0.0001);
        assertEquals(-5.0, NumberUtils.calculatePercentageChange(100, 95), 0.0001);
        assertEquals(0.0, NumberUtils.calculatePercentageChange(100, 100), 0.0001);
        assertEquals(0.0, NumberUtils.calculatePercentageChange(0, 100), 0.0001);
        assertEquals(10.0, NumberUtils.calculatePercentageChange(10, 11), 0.0001);
    }
    
    @Test
    public void testAverage() {
        assertEquals(0.0, NumberUtils.average(null), 0.0001);
        assertEquals(0.0, NumberUtils.average(new double[0]), 0.0001);
        assertEquals(5.0, NumberUtils.average(new double[] {5}), 0.0001);
        assertEquals(3.0, NumberUtils.average(new double[] {1, 2, 3, 4, 5}), 0.0001);
        assertEquals(2.5, NumberUtils.average(new double[] {1, 2, 3, 4}), 0.0001);
    }
    
    @Test
    public void testStandardDeviation() {
        assertEquals(0.0, NumberUtils.standardDeviation(null), 0.0001);
        assertEquals(0.0, NumberUtils.standardDeviation(new double[0]), 0.0001);
        assertEquals(0.0, NumberUtils.standardDeviation(new double[] {5}), 0.0001);
        assertEquals(1.41, NumberUtils.standardDeviation(new double[] {1, 2, 3, 4, 5}), 0.01);
    }
    
    @Test
    public void testVariance() {
        assertEquals(0.0, NumberUtils.variance(null), 0.0001);
        assertEquals(0.0, NumberUtils.variance(new double[0]), 0.0001);
        assertEquals(0.0, NumberUtils.variance(new double[] {5}), 0.0001);
        assertEquals(2.0, NumberUtils.variance(new double[] {1, 2, 3, 4, 5}), 0.01);
    }
    
    @Test
    public void testMax() {
        assertEquals(0.0, NumberUtils.max(null), 0.0001);
        assertEquals(0.0, NumberUtils.max(new double[0]), 0.0001);
        assertEquals(5.0, NumberUtils.max(new double[] {5}), 0.0001);
        assertEquals(5.0, NumberUtils.max(new double[] {1, 2, 3, 4, 5}), 0.0001);
        assertEquals(10.5, NumberUtils.max(new double[] {1.5, 10.5, 3.2, 4.8}), 0.0001);
    }
    
    @Test
    public void testMin() {
        assertEquals(0.0, NumberUtils.min(null), 0.0001);
        assertEquals(0.0, NumberUtils.min(new double[0]), 0.0001);
        assertEquals(5.0, NumberUtils.min(new double[] {5}), 0.0001);
        assertEquals(1.0, NumberUtils.min(new double[] {1, 2, 3, 4, 5}), 0.0001);
        assertEquals(1.5, NumberUtils.min(new double[] {1.5, 10.5, 3.2, 4.8}), 0.0001);
    }
    
    @Test
    public void testToInt() {
        assertEquals(0, NumberUtils.toInt(null, 0));
        assertEquals(0, NumberUtils.toInt("", 0));
        assertEquals(123, NumberUtils.toInt("123", 0));
        assertEquals(-123, NumberUtils.toInt("-123", 0));
        assertEquals(0, NumberUtils.toInt("abc", 0));
        assertEquals(5, NumberUtils.toInt("abc", 5));
    }
    
    @Test
    public void testToDouble() {
        assertEquals(0.0, NumberUtils.toDouble(null, 0.0), 0.0001);
        assertEquals(0.0, NumberUtils.toDouble("", 0.0), 0.0001);
        assertEquals(123.45, NumberUtils.toDouble("123.45", 0.0), 0.0001);
        assertEquals(-123.45, NumberUtils.toDouble("-123.45", 0.0), 0.0001);
        assertEquals(0.0, NumberUtils.toDouble("abc", 0.0), 0.0001);
        assertEquals(5.5, NumberUtils.toDouble("abc", 5.5), 0.0001);
    }
}