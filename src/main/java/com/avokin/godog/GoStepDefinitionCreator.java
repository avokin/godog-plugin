package com.avokin.godog;

import com.goide.psi.GoFile;
import com.goide.psi.GoMethodDeclaration;
import com.goide.psi.GoSimpleStatement;
import com.goide.psi.impl.GoElementFactory;
import com.google.common.collect.Iterables;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.Collection;

import static com.avokin.godog.GodogSnippetGenerator.STEP_DEFINITION_FILE_TEMPLATE;
import static com.avokin.godog.GodogSnippetGenerator.buildStepDefinitionDeclaration;

public class GoStepDefinitionCreator extends AbstractStepDefinitionCreator {
    private final static Logger LOG = Logger.getInstance(GoStepDefinitionCreator.class);

    @Override
    public @NotNull
    PsiFile createStepDefinitionContainer(@NotNull PsiDirectory psiDirectory, @NotNull String name) {
        return WriteAction.compute(() -> psiDirectory.createFile(name + ".go"));
    }

    @Override
    public boolean createStepDefinition(@NotNull GherkinStep gherkinStep, @NotNull PsiFile psiFile) {
        if (gherkinStep.getName() == null) {
            return false;
        }
        GoMethodDeclaration godogStepMethod = GodogUtil.findSuiteStepMethodDeclaration(psiFile);
        if (godogStepMethod == null) {
            // balloon with message about misconfiguration of godog
            LOG.error("Failed to create step definition");
            return false;
        }
        LocalSearchScope scope = new LocalSearchScope(psiFile);
        Collection<PsiReference> fileStepDefinitions = ReferencesSearch.search(godogStepMethod, scope).findAll();
        PsiElement anchor;
        if (fileStepDefinitions.size() == 0) {
            GoFile stepDefinitionFile = GoElementFactory.createFileFromText(gherkinStep.getProject(), STEP_DEFINITION_FILE_TEMPLATE);
            for (PsiElement element : stepDefinitionFile.getChildren()) {
                psiFile.add(element);
            }

            anchor = psiFile.findElementAt(stepDefinitionFile.getText().indexOf("{"));
            assert anchor != null;
        } else {
            PsiReference lastStepDefinitionReference = Iterables.getLast(fileStepDefinitions);
            anchor = lastStepDefinitionReference.getElement().getParent();
            anchor = PsiTreeUtil.getParentOfType(anchor, GoSimpleStatement.class);
            if (anchor == null) {
                LOG.error("Failed to create step definition");
                return false;
            }
        }

        String snippet = buildStepDefinitionDeclaration(gherkinStep.getName());
        PsiElement stepDefinition = GoElementFactory.createCallExpression(psiFile.getProject(), snippet);
        stepDefinition = PsiTreeUtil.getParentOfType(stepDefinition, GoSimpleStatement.class);
        if (stepDefinition == null) {
            LOG.error("Failed to create step definition");
            return false;
        }

        anchor.getParent().addAfter(stepDefinition, anchor);
        return true;
    }

    @Override
    public boolean validateNewStepDefinitionFileName(@NotNull Project project, @NotNull String s) {
        return true;
    }

    @Override
    public @NotNull
    String getDefaultStepFileName(@NotNull GherkinStep gherkinStep) {
        return "godog_test";
    }
}
