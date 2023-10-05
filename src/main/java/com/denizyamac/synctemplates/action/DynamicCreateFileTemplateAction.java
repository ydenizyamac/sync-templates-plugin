package com.denizyamac.synctemplates.action;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Properties;

public class DynamicCreateFileTemplateAction extends CreateFileFromTemplateAction {

    public DynamicCreateFileTemplateAction(String text, Icon icon) {
        super(text, text, icon);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory,
                               @NotNull CreateFileFromTemplateDialog.Builder builder) {

    }

    @Override
    protected String getActionName(PsiDirectory directory,
                                   @NotNull String newName, String templateName) {
        return "Create: " + newName;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory dir) {
        var project = dir.getProject();
        var defaultProperties = FileTemplateManager.getInstance(project).getDefaultProperties();
        var properties = new Properties(defaultProperties);
        PsiElement element;
        try {
            var dialog = new CreateFromTemplateDialog(
                    project, dir, template,
                    new AttributesDefaults(name).withFixedName(true),
                    properties
            );
            dialog.setTitle(name + " Variables");
            element = dialog
                    .create();
        } catch (IncorrectOperationException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e);
            return null;
        }

        return element != null ? element.getContainingFile() : null;
    }


}
