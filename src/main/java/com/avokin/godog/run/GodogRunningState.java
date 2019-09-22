package com.avokin.godog.run;

import com.goide.execution.testing.GoTestRunConfiguration;
import com.goide.execution.testing.GoTestRunningState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public class GodogRunningState extends GoTestRunningState {
    public GodogRunningState(@NotNull ExecutionEnvironment env, @NotNull Module module, @NotNull GoTestRunConfiguration configuration) {
        super(env, module, configuration);
    }
}
