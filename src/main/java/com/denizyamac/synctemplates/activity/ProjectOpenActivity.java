package com.denizyamac.synctemplates.activity;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ProjectOpenActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        PluginSettings.setRepositoryUrl(PluginConstants.BLOB_URL);
        PluginSettings.setConfigFileName(PluginConstants.CONFIG_FILE_NAME);
        GroupHelper.createMainMenu();

        TemplateHelper.getTemplates(false);
    }
}
