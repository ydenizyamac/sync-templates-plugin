package com.denizyamac.synctemplates.helper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileHelper {
    protected static final Logger LOG = Logger.getInstance(FileHelper.class);

    public static PsiFile createFileFromTemplate(String name, FileTemplate template, PsiDirectory dir) {
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
