package com.denizyamac.synctemplates.action;


import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.denizyamac.synctemplates.model.PluginConfig;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UpdateTemplatesAction extends AnAction {
    public UpdateTemplatesAction(String text, Icon icon) {
        super(text, text, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PluginConfig pluginConfig = TemplateHelper.readFromUrl(PluginConstants.Helper.getConfigUrl(), PluginConfig.class);
        if (pluginConfig != null) {
            PluginSettings.setConfig(pluginConfig);
            TemplateHelper.addAllTemplatesAndGroups(pluginConfig);
        } else SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog("Please Check Config File", "Config Error");
        });

    }

}