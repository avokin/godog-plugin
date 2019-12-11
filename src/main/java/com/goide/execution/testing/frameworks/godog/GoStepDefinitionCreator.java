package com.goide.execution.testing.frameworks.godog;

import com.goide.psi.*;
import com.goide.psi.impl.GoElementFactory;
import com.goide.util.Value;
import com.google.common.collect.Iterables;
import com.intellij.codeInsight.template.TemplateBuilderFactory;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

import java.util.Collection;

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
            GoFile stepDefinitionFile = GoElementFactory.createFileFromText(gherkinStep.getProject(), GodogSnippetGenerator.STEP_DEFINITION_FILE_TEMPLATE);
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

        String snippet = GodogSnippetGenerator.buildStepDefinitionDeclaration(gherkinStep.getName());
        PsiElement stepDefinition = GoElementFactory.createCallExpression(psiFile.getProject(), snippet);
        stepDefinition = PsiTreeUtil.getParentOfType(stepDefinition, GoSimpleStatement.class);
        if (stepDefinition == null) {
            LOG.error("Failed to create step definition");
            return false;
        }

        PsiElement addedStepDefinition = anchor.getParent().addAfter(stepDefinition, anchor);
        GoCallExpr stepDefinitionCall = PsiTreeUtil.findChildOfType(addedStepDefinition, GoCallExpr.class);
        assert stepDefinitionCall != null;
        runTemplateOnAddedStepDefinition(stepDefinitionCall);
        return true;
    }

    private void runTemplateOnAddedStepDefinition(@NotNull GoCallExpr call) {
        Project project = call.getProject();

        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, call.getContainingFile().getVirtualFile(), 0);
        Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
        assert editor != null;

        closeActiveTemplateBuilders(call.getContainingFile());

        TemplateBuilderImpl builder = (TemplateBuilderImpl) TemplateBuilderFactory.getInstance().createTemplateBuilder(call);

        // template over regex
        GoStringLiteral regexpElement = PsiTreeUtil.findChildOfType(call, GoStringLiteral.class);
        assert regexpElement != null;
        Value regexpElementValue = regexpElement.getValue();
        assert regexpElementValue != null;
        String regexpText = regexpElementValue.getString();
        TextRange rangeInRegexpLiteral = new TextRange(1, regexpElement.getTextLength() - 1);
        builder.replaceElement(regexpElement, rangeInRegexpLiteral, regexpText);

        // template over argument list
        Collection<GoParamDefinition> parameters = PsiTreeUtil.findChildrenOfType(call, GoParamDefinition.class);
        for (GoParamDefinition parameter : parameters) {
            PsiElement nameIdentifier = parameter.getNameIdentifier();
            assert nameIdentifier != null;
            builder.replaceElement(nameIdentifier, TextRange.create(0, nameIdentifier.getTextLength()), parameter.getName());
        }

        GoReturnStatement returnStatement = PsiTreeUtil.findChildOfType(call, GoReturnStatement.class);
        assert returnStatement != null;
        builder.setEndVariableBefore(returnStatement);

        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
        builder.run(editor, false);
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
