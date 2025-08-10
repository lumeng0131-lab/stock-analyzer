package com.stockanalyzer.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * StringUtils工具类的单元测试
 */
public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));
    }
    
    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));
    }
    
    @Test
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("\t\n\r"));
        assertFalse(StringUtils.isBlank("abc"));
        assertFalse(StringUtils.isBlank(" abc "));
    }
    
    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertFalse(StringUtils.isNotBlank("\t\n\r"));
        assertTrue(StringUtils.isNotBlank("abc"));
        assertTrue(StringUtils.isNotBlank(" abc "));
    }
    
    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0, 3));
        assertEquals("", StringUtils.substring("", 0, 3));
        assertEquals("", StringUtils.substring("abc", 0, 0));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("abc", StringUtils.substring("abc", 0, 4));
        assertEquals("bc", StringUtils.substring("abc", 1, 3));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("", StringUtils.substring("abc", 2, 1));
        assertEquals("abc", StringUtils.substring("abc", -1, 3));
    }
    
    @Test
    public void testJoinArray() {
        assertEquals("", StringUtils.join((String[]) null, ","));
        assertEquals("", StringUtils.join(new String[0], ","));
        assertEquals("abc", StringUtils.join(new String[] {"abc"}, ","));
        assertEquals("abc,def", StringUtils.join(new String[] {"abc", "def"}, ","));
        assertEquals("abc,def,ghi", StringUtils.join(new String[] {"abc", "def", "ghi"}, ","));
        assertEquals("abcdefghi", StringUtils.join(new String[] {"abc", "def", "ghi"}, null));
        assertEquals("abc-def-ghi", StringUtils.join(new String[] {"abc", "def", "ghi"}, "-"));
        assertEquals("abc,null,ghi", StringUtils.join(new String[] {"abc", null, "ghi"}, ","));
    }
    
    @Test
    public void testJoinList() {
        assertEquals("", StringUtils.join((List<String>) null, ","));
        assertEquals("", StringUtils.join(Arrays.asList(), ","));
        assertEquals("abc", StringUtils.join(Arrays.asList("abc"), ","));
        assertEquals("abc,def", StringUtils.join(Arrays.asList("abc", "def"), ","));
        assertEquals("abc,def,ghi", StringUtils.join(Arrays.asList("abc", "def", "ghi"), ","));
        assertEquals("abcdefghi", StringUtils.join(Arrays.asList("abc", "def", "ghi"), null));
        assertEquals("abc-def-ghi", StringUtils.join(Arrays.asList("abc", "def", "ghi"), "-"));
        assertEquals("abc,null,ghi", StringUtils.join(Arrays.asList("abc", null, "ghi"), ","));
    }
    
    @Test
    public void testSplit() {
        assertArrayEquals(new String[0], StringUtils.split(null, ","));
        assertArrayEquals(new String[0], StringUtils.split("", ","));
        assertArrayEquals(new String[] {"abc"}, StringUtils.split("abc", null));
        assertArrayEquals(new String[] {"abc", "def", "ghi"}, StringUtils.split("abc,def,ghi", ","));
        assertArrayEquals(new String[] {"abc", "def", "ghi"}, StringUtils.split("abc-def-ghi", "-"));
        assertArrayEquals(new String[] {"abc", "", "ghi"}, StringUtils.split("abc,,ghi", ","));
    }
    
    @Test
    public void testExtractAll() {
        assertTrue(StringUtils.extractAll(null, "\\d+").isEmpty());
        assertTrue(StringUtils.extractAll("", "\\d+").isEmpty());
        assertTrue(StringUtils.extractAll("abc", null).isEmpty());
        assertTrue(StringUtils.extractAll("abc", "").isEmpty());
        
        List<String> numbers = StringUtils.extractAll("abc123def456ghi", "\\d+");
        assertEquals(2, numbers.size());
        assertEquals("123", numbers.get(0));
        assertEquals("456", numbers.get(1));
        
        List<String> words = StringUtils.extractAll("abc123def456ghi", "[a-z]+");
        assertEquals(3, words.size());
        assertEquals("abc", words.get(0));
        assertEquals("def", words.get(1));
        assertEquals("ghi", words.get(2));
    }
    
    @Test
    public void testMatches() {
        assertFalse(StringUtils.matches(null, "\\d+"));
        assertFalse(StringUtils.matches("", "\\d+"));
        assertFalse(StringUtils.matches("abc", null));
        
        assertTrue(StringUtils.matches("123", "\\d+"));
        assertFalse(StringUtils.matches("abc", "\\d+"));
        assertTrue(StringUtils.matches("abc123", "[a-z]+\\d+"));
    }
    
    @Test
    public void testCapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Abc", StringUtils.capitalize("abc"));
        assertEquals("Abc", StringUtils.capitalize("Abc"));
        assertEquals("A", StringUtils.capitalize("a"));
    }
    
    @Test
    public void testUncapitalize() {
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("abc", StringUtils.uncapitalize("Abc"));
        assertEquals("abc", StringUtils.uncapitalize("abc"));
        assertEquals("a", StringUtils.uncapitalize("A"));
    }
}