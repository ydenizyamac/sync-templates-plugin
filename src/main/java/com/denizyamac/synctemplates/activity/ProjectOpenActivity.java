package com.denizyamac.synctemplates.activity;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.denizyamac.synctemplates.model.PluginConfig;
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

        var config = PluginSettings.getConfig();
        if (config == null) {
            config = TemplateHelper.readFromUrl(PluginConstants.Helper.getConfigUrl(), PluginConfig.class);
        }
        if (config != null) {
            PluginSettings.setConfig(config);
            TemplateHelper.addAllTemplatesAndGroups(config);
        } /*else SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog("Please check config file", "Config Error");
        });*/

    }
}
