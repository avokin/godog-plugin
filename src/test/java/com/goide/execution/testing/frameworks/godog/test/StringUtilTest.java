package com.goide.execution.testing.frameworks.godog.test;

import com.goide.execution.testing.frameworks.godog.StringUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilTest {
    @Test
    public void testEscapeToRegex() {
        assertEquals("I have 9 cucumber\\(s)", StringUtil.escapeToRegex("I have 9 cucumber(s)"));
        assertEquals("I have 9\\$", StringUtil.escapeToRegex("I have 9$"));
    }

    @Test
    public void testEscapeSlashes() {
        assertEquals("\\\\d+\\\\.\\\\d+", StringUtil.escapeSlashes("\\d+\\.\\d+"));
    }
}
