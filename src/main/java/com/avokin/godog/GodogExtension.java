package com.avokin.godog;

import com.goide.GoFileType;
import com.goide.execution.testing.GoTestFramework;
import com.goide.psi.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.*;

import static com.avokin.godog.GodogUtil.findSuiteStepMethodDeclaration;

public class GodogExtension extends AbstractCucumberExtension {
    static {
        GoTestFramework.all().add(GodogFramework.INSTANCE);
    }

    @Override
    public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
        return parent instanceof PsiDirectory && child instanceof GoFile;
    }

    @Override
    public boolean isWritableStepLikeFile(@NotNull PsiElement element, @NotNull PsiElement parent) {
        String filePath = element.getContainingFile().getVirtualFile().getPath();
        String projectPath = element.getProject().getBasePath();
        if (projectPath == null) {
            return false;
        }
        return FileUtil.isAncestor(projectPath, filePath, true);
    }

    @NotNull
    @Override
    public BDDFrameworkType getStepFileType() {
        return new BDDFrameworkType(GoFileType.INSTANCE);
    }

    @NotNull
    @Override
    public StepDefinitionCreator getStepDefinitionCreator() {
        return new GoStepDefinitionCreator();
    }

    @NotNull
    @Override
    public Collection<String> getGlues(@NotNull GherkinFile gherkinFile, Set<String> set) {
        return Collections.emptySet();
    }

    @Override
    public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile psiFile, @NotNull Module module) {
        GoMethodDeclaration godogStepMethod = psiFile != null ? findSuiteStepMethodDeclaration(psiFile) : null;
        if (godogStepMethod == null) {
            return Collections.emptyList();
        }

        List<AbstractStepDefinition> result = new ArrayList<>();
        Collection<PsiReference> findUsageQuery = ReferencesSearch.search(godogStepMethod).findAll();
        for (PsiReference methodStepReference : findUsageQuery) {
            PsiElement stepDefinitionRegistrarCandidate = methodStepReference.getElement().getParent();
            if (stepDefinitionRegistrarCandidate instanceof GoCallExpr) {
                GoCallExpr callExpr = (GoCallExpr) stepDefinitionRegistrarCandidate;
                List<GoExpression> expressionList = callExpr.getArgumentList().getExpressionList();

                if (expressionList.size() > 1 && expressionList.get(0) instanceof GoStringLiteral) {
                    result.add(new GoStepDefinition((GoStringLiteral) expressionList.get(0)));
                }
            }
        }

        return result;
    }

    @Override
    public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull GherkinFile gherkinFile) {
        final Module module = ModuleUtilCore.findModuleForPsiElement(gherkinFile);
        if (module == null) {
            return Collections.emptySet();
        }
        List<AbstractStepDefinition> stepDefinitions = loadStepsFor(gherkinFile, module);

        Set<PsiFile> result = new HashSet<>();
        for (AbstractStepDefinition stepDef : stepDefinitions) {
            PsiElement stepDefElement = stepDef.getElement();
            if (stepDefElement != null) {
                final PsiFile psiFile = stepDefElement.getContainingFile();
                PsiDirectory psiDirectory = psiFile.getParent();
                if (psiDirectory != null && isWritableStepLikeFile(psiFile, psiDirectory)) {
                    result.add(psiFile);
                }
            }
        }
        return result;
    }
}
