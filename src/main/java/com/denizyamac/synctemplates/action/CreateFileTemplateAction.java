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

import java.util.Properties;

import static com.intellij.util.PlatformIcons.FILE_ICON;

public class CreateFileTemplateAction extends CreateFileFromTemplateAction {

    public CreateFileTemplateAction() {
        super("create file", "desc", FILE_ICON);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory,
                               CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("New My File")
                .addKind("File", FILE_ICON, "Example");

    }

    @Override
    protected String getActionName(PsiDirectory directory,
                                   @NotNull String newName, String templateName) {
        return "Create My Class: " + newName;
    }

    @Override
    protected PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory dir) {
        var project = dir.getProject();
        var defaultProperties = FileTemplateManager.getInstance(project).getDefaultProperties();
        var properties = new Properties(defaultProperties);
        PsiElement element = null;
        try {
            element = new CreateFromTemplateDialog(
                    project, dir, template,
                    new AttributesDefaults(name).withFixedName(true),
                    properties
            ).create();
        } catch (IncorrectOperationException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e);
            return null;
        }

        return element != null ? element.getContainingFile() : null;
    }


}