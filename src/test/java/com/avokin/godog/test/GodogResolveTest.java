package com.avokin.godog.test;

import com.goide.psi.GoStringLiteral;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;

public class GodogResolveTest extends BasePlatformTestCase {
    public void testResolveToCall() {
        myFixture.copyDirectoryToProject("", "");
        myFixture.configureByFile("test.feature");

        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        GherkinStep step = PsiTreeUtil.getParentOfType(element, GherkinStep.class);
        assertNotNull(step);
        PsiElement resolved = step.getReferences()[0].resolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof GoStringLiteral);
    }

    @Override
    protected String getTestDataPath() {
        PathManager.getHomePath();
        return GodogTestUtil.getDataPath(getClass());
    }
}
