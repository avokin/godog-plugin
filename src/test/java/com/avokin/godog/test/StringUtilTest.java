package com.avokin.godog.test;

import com.avokin.godog.StringUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {
    @Test
    public void testEscapeToRegex() {
        assertEquals("I have 9 cucumber\\(s)", StringUtil.escapeToRegex("I have 9 cucumber(s)"));
        assertEquals("I have 9\\$", StringUtil.escapeToRegex("I have 9$"));
    }
}
