package com.avokin.godog;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public class GoStepDefinitionCreator extends AbstractStepDefinitionCreator {
    @Override
    public @NotNull PsiFile createStepDefinitionContainer(@NotNull PsiDirectory psiDirectory, @NotNull String name) {
        return WriteAction.compute(() -> psiDirectory.createFile(name + ".go"));
    }

    @Override
    public boolean createStepDefinition(@NotNull GherkinStep gherkinStep, @NotNull PsiFile psiFile) {
        return false;
    }

    @Override
    public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String s) {
        return true;
    }

    @Override
    public @NotNull String getDefaultStepFileName(@NotNull GherkinStep gherkinStep) {
        return "my_steps.go";
    }
}
