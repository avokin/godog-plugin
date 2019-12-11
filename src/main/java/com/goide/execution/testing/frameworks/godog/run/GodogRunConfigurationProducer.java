package com.goide.execution.testing.frameworks.godog.run;

import com.goide.execution.testing.frameworks.godog.GodogFramework;
import com.goide.execution.GoRunUtil;
import com.goide.execution.testing.GoTestRunConfiguration;
import com.goide.execution.testing.GoTestRunConfigurationProducerBase;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;

public class GodogRunConfigurationProducer extends GoTestRunConfigurationProducerBase {
    protected GodogRunConfigurationProducer() {
        super(GodogFramework.INSTANCE);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull GoTestRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref sourceElement) {
        PsiElement element = (PsiElement) sourceElement.get();
        if (element == null) {
            return false;
        }
        if (element.getContainingFile() instanceof GherkinFile) {
            VirtualFile file = element.getContainingFile().getVirtualFile();
            String directoryPath = file.getParent().getPath();
            configuration.setTestFramework(GodogFramework.INSTANCE);
            configuration.setFilePathsString(file.getPath());
            configuration.setWorkingDirectory(directoryPath);
            configuration.setKind(GoTestRunConfiguration.Kind.FILE);
            configuration.setGoToolParams(GoRunUtil.filterOutInstallParameter(configuration.getGoToolParams()));
            GherkinScenario scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario.class);
            if (scenario != null) {
                configuration.setPattern(scenario.getScenarioName());
            }

            configuration.setGeneratedName();
            return true;
        }
        if (element instanceof PsiDirectory) {
            VirtualFile dir = ((PsiDirectory) element).getVirtualFile();
            if (!FileTypeIndex.containsFileOfType(GherkinFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(element.getProject(), dir, true))) {
                return false;
            }
            String directoryPath = dir.getPath();
            configuration.setTestFramework(GodogFramework.INSTANCE);
            configuration.setDirectoryPath(directoryPath);
            configuration.setWorkingDirectory(directoryPath);
            configuration.setKind(GoTestRunConfiguration.Kind.DIRECTORY);
            configuration.setGoToolParams(GoRunUtil.filterOutInstallParameter(configuration.getGoToolParams()));
            configuration.setGeneratedName();

            return true;
        }
        return false;
    }
}
