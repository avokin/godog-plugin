package com.goide.execution.testing.frameworks.godog;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GodogSnippetGenerator {
    private static final String DEFAULT_PARAMETER_TYPE = "string";

    private static final String[] PARAMETER_REGEX = new String[] {"<[^>]*>", "\"[^\"]*\"", "'[^']*'", "\\d+\\.\\d+", "\\d+"};
    private static final String[] PARAMETER_TYPES = new String[] {"string", "string", "string", "float32", "int"};

    private static final String ALL_TYPES = Arrays.stream(PARAMETER_REGEX).map(s -> "(" + s + ")").collect(Collectors.joining("|"));

    static final String STEP_DEFINITION_FILE_TEMPLATE =
            "package main\n" +
            "\n" +
            "import (\n" +
            "  \"github.com/DATA-DOG/godog\"\n" +
            ")\n" +
            "\n" +
            "func FeatureContext(s *godog.Suite) {\n" +
            "}\n";

    @NotNull
    public static String buildStepDefinitionDeclaration(@NotNull String stepName) {
        String stepRegexp = StringUtil.escapeToRegex(stepName);

        for (String regex : PARAMETER_REGEX) {
            String escapedRegex = StringUtil.escapeSlashes("(" + regex + ")");
            stepRegexp = stepRegexp.replaceAll(regex, escapedRegex);
        }
        String stepDefinitionFunction = buildStepDefinitionFunction(stepName);
        return String.format("s.Step(`%s`, %s)", stepRegexp, stepDefinitionFunction);
    }

    @NotNull
    private static String buildStepDefinitionFunction(@NotNull String stepName) {
        StringBuilder result = new StringBuilder("func(");

        Pattern parameterPattern = Pattern.compile(ALL_TYPES);
        Matcher m = parameterPattern.matcher(stepName);
        int count = 1;
        while (m.find()) {
            String matchedValue = stepName.substring(m.start(), m.end());
            String type = DEFAULT_PARAMETER_TYPE;
            for (int i = 0; i < PARAMETER_REGEX.length; i++) {
                if (matchedValue.matches(PARAMETER_REGEX[i])) {
                    type = PARAMETER_TYPES[i];
                    break;
                }
            }
            if (count > 1) {
                result.append(", ");
            }
            result.append("arg").append(count).append(" ").append(type);
            count++;
        }
        result.append(") error {\n" +
                "  return godog.ErrPending\n" +
                "}");
        return result.toString();
    }
}
