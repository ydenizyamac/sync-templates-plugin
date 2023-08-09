package com.denizyamac.synctemplates.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DynamicAction extends AnAction {
    private IActionToPerform actionToPerform;

    public DynamicAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    public void setActionToPerform(IActionToPerform action) {
        this.actionToPerform = action;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        this.actionToPerform.action(e);
        // Action logic goes here
    }

    public interface IActionToPerform {
        void action(AnActionEvent e);
    }
}