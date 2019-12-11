package com.goide.execution.testing.frameworks.godog;

import org.jetbrains.annotations.NotNull;

public class StringUtil {
    private static final String CHARS_MUST_BE_ESCAPED_IN_REGEX = "([\\^$";

    @NotNull
    public static String escapeToRegex(@NotNull String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; text.length() > i; i++) {
            char c = text.charAt(i);
            if (CHARS_MUST_BE_ESCAPED_IN_REGEX.indexOf(c) >= 0 || isSurroundedByDigits(text, i)) {
                result.append("\\");
            }
            result.append(c);
        }

        return result.toString();
    }

    @NotNull
    public static String escapeSlashes(@NotNull String text) {
        return text.replaceAll("\\\\", "\\\\\\\\");
    }

    private static boolean isSurroundedByDigits(@NotNull String text, int offset) {
        return offset > 0 && offset < text.length() - 1 &&
                Character.isDigit(text.charAt(offset - 1)) && Character.isDigit(offset + 1);
    }
}
