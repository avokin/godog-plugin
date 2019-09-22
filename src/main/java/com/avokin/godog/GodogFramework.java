package com.avokin.godog;

import com.avokin.godog.run.GodogRunningState;
import com.goide.execution.testing.GoTestFramework;
import com.goide.execution.testing.GoTestRunConfiguration;
import com.goide.execution.testing.GoTestRunningState;
import com.goide.execution.testing.frameworks.gotest.GotestEventsConverter;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import static com.avokin.godog.GodogUtil.isGodogEnabledForModule;

public class GodogFramework extends GoTestFramework {
    public static final GodogFramework INSTANCE = new GodogFramework();

    @NotNull
    @Override
    public String getName() {
        return "Godog";
    }

    @Override
    public boolean isAvailable(@Nullable Module module) {
        return isGodogEnabledForModule(module);
    }

    @Override
    public boolean isAvailableOnFile(@Nullable PsiFile psiFile) {
        return psiFile != null && psiFile.getFileType() == GherkinFileType.INSTANCE;
    }

    @Override
    public boolean isAvailableOnFunction(@Nullable GoFunctionOrMethodDeclaration goFunctionOrMethodDeclaration) {
        return false;
    }

    @NotNull
    @Override
    protected GoTestRunningState newRunningState(@NotNull ExecutionEnvironment executionEnvironment, @NotNull Module module, @NotNull GoTestRunConfiguration goTestRunConfiguration) {
        return new GodogRunningState(executionEnvironment, module, goTestRunConfiguration);
    }

    @NotNull
    @Override
    public OutputToGeneralTestEventsConverter createTestEventsConverter(@NotNull String s, @NotNull TestConsoleProperties testConsoleProperties, @Nullable Module module) {
        return new GotestEventsConverter(s, testConsoleProperties);
    }

    @NotNull
    @Override
    public String getPackageConfigurationName(@NotNull String packageName) {
        return "Godog";
    }
}
