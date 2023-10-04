package com.denizyamac.synctemplates.action;

import com.denizyamac.synctemplates.ui.View;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
}
