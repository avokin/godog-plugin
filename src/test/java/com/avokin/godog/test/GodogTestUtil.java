package com.avokin.godog.test;

import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

class GodogTestUtil {
    @NotNull
    static String getDataPath(@NotNull final Class s) {
        final String classFullPath = getClassFullPath(s);
        return classFullPath.substring(0, classFullPath.lastIndexOf("/classes/")) + "/resources/test/testdata";
    }

    private static String getClassFullPath(@NotNull final Class s) {
        String name = s.getSimpleName() + ".class";
        final URL url = s.getResource(name);

        return URLUtil.decode(url.getPath());
    }
}
