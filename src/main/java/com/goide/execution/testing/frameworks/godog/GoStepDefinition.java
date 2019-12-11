package com.goide.execution.testing.frameworks.godog;

import com.goide.psi.GoStringLiteral;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.Collections;
import java.util.List;

public class GoStepDefinition extends AbstractStepDefinition {
    GoStepDefinition(@NotNull GoStringLiteral stepRegexp) {
        super(stepRegexp);
    }

    @Override
    public List<String> getVariableNames() {
        return Collections.emptyList();
    }

    @Override
    protected @Nullable String getCucumberRegexFromElement(PsiElement psiElement) {
        return getGoCall().getDecodedText();
    }

    private GoStringLiteral getGoCall() {
        return (GoStringLiteral) getElement();
    }
}
