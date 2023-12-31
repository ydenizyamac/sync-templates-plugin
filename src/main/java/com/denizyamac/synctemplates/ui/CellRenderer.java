package com.denizyamac.synctemplates.ui;


import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.model.ActionOrGroup;
import com.denizyamac.synctemplates.model.ActionOrGroupTypeEnum;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CellRenderer implements TreeCellRenderer, ActionListener {

    private JLabel label;
    //private JButton button;
    private JPanel renderer;

    public CellRenderer() {
        renderer = new JPanel(new GridLayout(1, 1));

        Icon icon = new ImageIcon("");
        label = new JLabel();
        //button = new JButton("Click Me!");
        //button.addActionListener(this);
        renderer.add(label);
        //renderer.add(button);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof ActionOrGroup) {
                ActionOrGroup leafObj = (ActionOrGroup) userObject;
                label.setText(leafObj.getName());

                if (leafObj.getType() == ActionOrGroupTypeEnum.ACTION) {
                    label.setIcon(GroupHelper.getIconFromResource("fileIcon.png"));
                } else {
                    label.setIcon(null);
                }
                renderer.setEnabled(tree.isEnabled());
                return renderer;
            } else if (userObject instanceof String) {
                label.setText((String) userObject);
                return renderer;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle the button click event here
    }
}