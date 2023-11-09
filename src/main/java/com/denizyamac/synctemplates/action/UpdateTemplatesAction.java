package com.denizyamac.synctemplates.action;


import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UpdateTemplatesAction extends AnAction {
    public UpdateTemplatesAction(String text, Icon icon) {
        super(text, text, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TemplateHelper.getTemplates(true);
    }

}