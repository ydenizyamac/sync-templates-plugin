package com.denizyamac.synctemplates.ui;

import com.denizyamac.synctemplates.model.ActionOrGroup;
import com.denizyamac.synctemplates.model.TemplateInput;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateParseException;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.fileTemplates.actions.AttributesDefaults;
import com.intellij.ide.fileTemplates.ui.CreateFromTemplatePanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

public class CreateMultipleFromTemplateDialog extends DialogWrapper {
    private static final Logger LOG = Logger.getInstance(com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog.class);
    @NotNull
    private final Project myProject;
    private final List<PsiElement> myCreatedElements;
    private final CreateFromTemplatePanel myAttrPanel;
    private final JComponent myAttrComponent;
    @NotNull
    private final List<FileTemplate> myTemplates;
    private final Properties myDefaultProperties;
    private final Map<String, TemplateInput> myTemplateInputs;
    private final @NotNull ActionOrGroup myItem;
    private final List<PsiDirectory> myDirectories;
    private final String myModulePath;

    public CreateMultipleFromTemplateDialog(@NotNull Project project,
                                            @NotNull ActionOrGroup item,
                                            @NotNull Map<String, TemplateInput> templateInputs,
                                            @NotNull String modulePath) {
        super(project, true);
        FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
        myProject = project;
        myTemplateInputs = templateInputs;
        myItem = item;
        myTemplates = new ArrayList<>();
        myDirectories = new ArrayList<>();
        myCreatedElements = new ArrayList<>();
        myDefaultProperties = new Properties(fileTemplateManager.getDefaultProperties());
        myModulePath = modulePath;
        myDefaultProperties.setProperty(FileTemplate.ATTRIBUTE_NAME, "a");
        myDefaultProperties.setProperty(FileTemplate.ATTRIBUTE_FILE_NAME, "a");
        myDefaultProperties.setProperty(FileTemplate.ATTRIBUTE_PACKAGE_NAME, "a");
        var attributesDefaults = new AttributesDefaults("a");
        attributesDefaults.add(FileTemplate.ATTRIBUTE_NAME, "a");
        attributesDefaults.add(FileTemplate.ATTRIBUTE_FILE_NAME, "a");
        attributesDefaults.add(FileTemplate.ATTRIBUTE_PACKAGE_NAME, "a");
        for (var file : item.getFiles()) {
            var uName = item.getTemplateFileUniqueName(file);
            myTemplates.add(fileTemplateManager.getInternalTemplate(uName));
            myDirectories.add(getDirectory(templateInputs.get(file).getTPackage()));
        }


        setTitle(IdeBundle.message("title.new.from.template", item.getName()));


        boolean mustEnterName = false;
        List<String> unsetAttributes = new ArrayList<>();

        for (var i = 0; i < item.getFiles().length; i++) {
            var template = myTemplates.get(i);
            var input = myTemplateInputs.get(myItem.getFiles()[i]);
            if (input.getTInclude()) {
                try {
                    unsetAttributes.addAll(Arrays.stream(template.getUnsetAttributes(myDefaultProperties, project)).filter(p -> !unsetAttributes.contains(p)).toList());
                } catch (FileTemplateParseException e) {
                    showErrorDialog(e, template);
                }
            }
        }
        myAttrPanel = new CreateFromTemplatePanel(unsetAttributes.toArray(String[]::new), mustEnterName, attributesDefaults);
        myAttrComponent = myAttrPanel.getComponent();
        init();
    }

    public List<PsiElement> create() {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            for (var i = 0; i < myTemplates.size(); i++) {
                var template = myTemplates.get(i);
                var directory = myDirectories.get(i);
                var input = myTemplateInputs.get(myItem.getFiles()[i]);
                if (input.getTInclude()) {
                    doCreate(template, input.getTName(), directory);
                }
            }
            Disposer.dispose(getDisposable());
            return myCreatedElements;
        }
        if (myAttrPanel != null) {
            if (myAttrPanel.hasSomethingToAsk()) {
                show();
                return myCreatedElements;
            }
            for (var i = 0; i < myTemplates.size(); i++) {
                var template = myTemplates.get(i);
                var directory = myDirectories.get(i);
                var input = myTemplateInputs.get(myItem.getFiles()[i]);
                if (input.getTInclude()) {
                    doCreate(template, input.getTName(), directory);
                }
            }
        }
        close(DialogWrapper.OK_EXIT_CODE);
        return myCreatedElements;
    }

    @Override
    protected void doOKAction() {
        for (var i = 0; i < myTemplates.size(); i++) {
            var template = myTemplates.get(i);
            var directory = myDirectories.get(i);
            var input = myTemplateInputs.get(myItem.getFiles()[i]);
            if (input.getTInclude()) {
                doCreate(template, input.getTName(), directory);
            }
        }
        if (!myCreatedElements.isEmpty()) {
            super.doOKAction();
        }
    }

    private void doCreate(FileTemplate myTemplate, @Nullable String fileName, PsiDirectory myDirectory) {
        try {
            Properties properties = myAttrPanel.getProperties(myDefaultProperties);
            myCreatedElements.add(createFile(fileName, myTemplate, properties, myDirectory));
        } catch (Exception e) {
            showErrorDialog(e, myTemplate);
        }
    }

    private PsiDirectory getDirectory(String path) {
        PsiManager psiManager = PsiManager.getInstance(myProject);
        Path mdPath = Paths.get(myModulePath);
        Path fPath = Paths.get(path);
        File _file = new File(mdPath.resolve(fPath).toString());
        VirtualFile directory = LocalFileSystem.getInstance().findFileByIoFile(_file);
        assert directory != null;
        return psiManager.findDirectory(directory);
    }

    private String getPackage(String path) {
        Path base = Paths.get(Objects.requireNonNull(myProject.getBasePath()));
        Path file = Paths.get(path);
        return base.relativize(file).toString();
    }

    private @NotNull PsiElement createFile(@Nullable String fileName,
                                           @NotNull FileTemplate template,
                                           @NotNull Properties properties,
                                           @NotNull PsiDirectory directory) throws Exception {
        FileTemplateUtil.fillDefaultProperties(myDefaultProperties, directory);
        if (fileName != null) {
            var fileNameWithoutExtension = fileName.split("\\.")[0];
            properties.setProperty(FileTemplate.ATTRIBUTE_NAME, fileNameWithoutExtension);
            properties.setProperty(FileTemplate.ATTRIBUTE_FILE_NAME, fileNameWithoutExtension);
            //properties.setProperty(FileTemplate.ATTRIBUTE_PACKAGE_NAME, getPackage(directory.getVirtualFile().getPath()));
            String newName = FileTemplateUtil.mergeTemplate(properties, fileName, false);
            CreateFileAction.MkDirs mkDirs = WriteAction.compute(() -> new CreateFileAction.MkDirs(newName, directory));
            return FileTemplateUtil.createFromTemplate(template, mkDirs.newName, properties, mkDirs.directory);
        }
        return FileTemplateUtil.createFromTemplate(template, null, properties, directory);
    }

    public Properties getEnteredProperties() {
        return myAttrPanel.getProperties(new Properties());
    }

    private void showErrorDialog(final Exception e, FileTemplate template) {
        LOG.info(e);
        Messages.showMessageDialog(myProject, filterMessage(e.getMessage(), template), getErrorMessage(template), Messages.getErrorIcon());
    }

    private @NlsContexts.DialogTitle String getErrorMessage(FileTemplate template) {
        return FileTemplateUtil.findHandler(template).getErrorMessage();
    }

    private @NlsContexts.DialogMessage @NotNull String filterMessage(@NlsContexts.DialogMessage String message, FileTemplate template) {
        if (message == null) {
            message = IdeBundle.message("dialog.message.unknown.error");
        }

        @NonNls String ioExceptionPrefix = "java.io.IOException:";
        if (message.startsWith(ioExceptionPrefix)) {
            return message.substring(ioExceptionPrefix.length());
        }
        if (message.contains("File already exists")) {
            return message;
        }

        return IdeBundle.message("error.unable.to.parse.template.message", template.getName(), message);
    }

    @Override
    protected JComponent createCenterPanel() {
        myAttrPanel.ensureFitToScreen(200, 200);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(myAttrComponent, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                JBUI.emptyInsets(), 0, 0));
        return centerPanel;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return IdeFocusTraversalPolicy.getPreferredFocusedComponent(myAttrComponent);
    }
}
