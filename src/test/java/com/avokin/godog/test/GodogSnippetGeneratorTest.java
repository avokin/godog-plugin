package com.avokin.godog.test;

import com.avokin.godog.GodogSnippetGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GodogSnippetGeneratorTest {
    @Test
    public void testSnippetWithoutParameters() {
        assertEquals(
                "s.Step(`my simple step`, func() error {\n" +
                "  return nil\n" +
                "})",
                GodogSnippetGenerator.buildStepDefinitionDeclaration("my simple step")
        );
    }

    @Test
    public void testSnippetWithParameters() {
        assertEquals(
                "s.Step(`step with (\"[^\"]*\") and ('[^']*') and (\\d+) and (\\d+\\.\\d+)`, func(arg1 string, arg2 string, arg3 int, arg4 float32) error {\n" +
                "  return nil\n" +
                "})",
                GodogSnippetGenerator.buildStepDefinitionDeclaration("step with \"string\" and 'string' and 10 and 10.2")
        );
    }
}
