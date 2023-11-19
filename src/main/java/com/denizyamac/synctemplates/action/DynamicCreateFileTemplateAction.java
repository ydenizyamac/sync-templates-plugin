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
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
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

    /* private void doCreate(@Nullable String fileName)  {
         try {
             Properties properties = myAttrPanel.getProperties(myDefaultProperties);
             for (FileTemplate child : myTemplate.getChildren()) {
                 createFile(child.getFileName(), child, properties);
             }
             String mainFileName = StringUtil.isEmpty(myTemplate.getFileName()) ? fileName : myTemplate.getFileName();
             myCreatedElement = createFile(mainFileName, myTemplate, properties);
         }
         catch (Exception e) {
             showErrorDialog(e);
         }
     }
 */
    private static void createFileFromTemplate(Project project, PsiDirectory targetDirectory, String fileName, String templateName, Properties properties) {
        try {
            // Get the file template manager
            FileTemplateManager templateManager = FileTemplateManager.getInstance(project);

            // Get the file template by name
            FileTemplate fileTemplate = templateManager.getJ2eeTemplate(templateName);

            // Create a Properties object to provide variables for the template
            //Properties properties = new Properties();

            // Add any variables you want to pass to the template (if needed)
            // properties.setProperty("KEY", "VALUE");

            // Generate content from the template using the specified properties
            String content = fileTemplate.getText(properties);

            // Create a new PsiFile from the generated content
            PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
            /*PsiFile psiFile = fileFactory.createFileFromText(fileName, , (CharSequence) content);

            // Add the new file to the specified directory
            PsiFile createdFile = (PsiFile) targetDirectory.add(psiFile);

            // Refresh the project to ensure the new file is recognized
            VirtualFile virtualFile = createdFile.getVirtualFile();
            virtualFile.refresh(false, false);
            */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
