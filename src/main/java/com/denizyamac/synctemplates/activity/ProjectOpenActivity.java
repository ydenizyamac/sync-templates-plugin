package com.denizyamac.synctemplates.activity;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.denizyamac.synctemplates.model.Template;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProjectOpenActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        PluginSettings.setRepositoryUrl(PluginConstants.BLOB_URL);
        PluginSettings.setConfigFileName(PluginConstants.CONFIG_FILE_NAME);
        GroupHelper.createMainMenu();

        var directorships = TemplateHelper.getDirectorships(false);
        if (directorships != null) {
            Template[] templates = PluginSettings.getTemplates();
            if (templates == null) {
                templates = TemplateHelper.getAllTemplates(directorships);
            }
            if (templates != null) {
                TemplateHelper.addAllTemplatesAndGroups(directorships);
            }
        } else SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog("Please Check Config File", "Config Error");
        });
    }
}
