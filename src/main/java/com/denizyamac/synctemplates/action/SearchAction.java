package com.denizyamac.synctemplates.action;

import com.denizyamac.synctemplates.ui.Presenter;
import com.denizyamac.synctemplates.ui.View;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;

public class SearchAction extends AnAction {
    public SearchAction(String text, Icon icon) {
        super(text, text, icon);
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = project.getBasePath();
        TreePath treePath = ProjectView.getInstance(project).getCurrentProjectViewPane().getSelectedPath();
        if (treePath.getPath().length < 6) {
            Messages.showErrorDialog("Please choose a package", "Error");
        } else {
            StringBuilder selectedPackageName = new StringBuilder(treePath.getPath()[5].toString());
            for (int i = 6; i < treePath.getPath().length; i++) {
                selectedPackageName.append(".").append(treePath.getPath()[i]);
            }
            View view = new View();
            new Presenter(view, selectedPackageName.toString(), projectPath);
        }

    }
}
