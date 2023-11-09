package com.denizyamac.synctemplates.action;

import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.ui.View;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SearchAction extends AnAction {
    public SearchAction(String text, Icon icon) {
        super(text, text, icon);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        View view = new View(e);
        view.pack();
        view.show();
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        // Hide the action if the clicked folder is not a Java package
        presentation.setEnabled(GroupHelper.isPackage(e));
    }
}