package com.denizyamac.synctemplates.action;


import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.denizyamac.synctemplates.model.Directorship;
import com.denizyamac.synctemplates.model.Template;
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
        Directorship[] directorships = TemplateHelper.getDirectorships(true);
        if (directorships != null) {
            Template[] templates = TemplateHelper.getAllTemplates(directorships);
            if (templates != null) {
                TemplateHelper.addAllTemplatesAndGroups(directorships, true);
                SwingUtilities.invokeLater(() -> Messages.showInfoMessage("Templates Updated", "Info"));
            }
        } else SwingUtilities.invokeLater(() -> Messages.showErrorDialog("Please Check Config File", "Config Error"));
    }

}