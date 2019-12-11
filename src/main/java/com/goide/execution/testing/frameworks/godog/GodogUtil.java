package com.goide.execution.testing.frameworks.godog;

import com.goide.psi.GoMethodDeclaration;
import com.goide.stubs.index.GoMethodIndex;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

class GodogUtil {
    private static final String GODOG_SUITE_FQN = "godog.Suite";
    private static final String GODOG_SUITE_STEP_FQN = "Suite.Step";

    static GoMethodDeclaration findSuiteStepMethodDeclaration(@NotNull PsiElement context) {
        PsiFile file = context.getContainingFile();
        return CachedValuesManager.getCachedValue(file,
                () -> CachedValueProvider.Result.create(doFindSuiteStepMethodDeclaration(file.getProject()), file)
        );
    }

    static boolean isGodogEnabledForModule(@Nullable Module module) {
        if (module == null) {
            return false;
        }
        // ToDo: cache, use module scope
        GoMethodDeclaration result = doFindSuiteStepMethodDeclaration(module.getProject());
        return result != null;
    }

    private static GoMethodDeclaration doFindSuiteStepMethodDeclaration(@NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        Collection<GoMethodDeclaration> suiteMethods = GoMethodIndex.find(GODOG_SUITE_FQN, project, scope, null);

        for (GoMethodDeclaration godogStepMethodCandidate : suiteMethods) {
            String qualifiedName = godogStepMethodCandidate.getQualifiedName();
            if (qualifiedName != null && qualifiedName.equals(GODOG_SUITE_STEP_FQN)) {
                return godogStepMethodCandidate;
            }
        }
        return null;
    }
}
