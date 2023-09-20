package com.denizyamac.synctemplates.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@Getter
public class View extends DialogWrapper {
    private JTextField modelNameTextField, searchTextField;
    @Getter(AccessLevel.PRIVATE)
    private DefaultMutableTreeNode root;
    @Getter(AccessLevel.PRIVATE)
    private JTree tree;

    public View() {
        super(true);
        setTitle("Templates");
        setSize(650, 500);
        init();
    }

    public void setOnKeyPress(Runnable runnable) {
        searchTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                runnable.run();
            }
        });
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel jPanel = new JPanel(null);
        root = new DefaultMutableTreeNode("Templates");
        tree = new Tree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scrollPane = new JBScrollPane(tree);
        scrollPane.setBounds(0, 0, 300, 300);
        jPanel.add(scrollPane);

        JLabel modelNameLabel = new JLabel("Name: ");
        modelNameLabel.setBounds(380, 60, 80, 40);
        jPanel.add(modelNameLabel);


        modelNameTextField = new JTextField();
        modelNameTextField.setBounds(420, 60, 150, 40);
        jPanel.add(modelNameTextField);

        JLabel searchLabel = new JLabel("Search: ");
        modelNameLabel.setBounds(370, 10, 50, 40);
        jPanel.add(searchLabel);


        searchTextField = new JTextField();
        searchTextField.setBounds(420, 10, 150, 40);
        jPanel.add(searchTextField);

        return jPanel;
    }

    public void expandRoot() {
        tree.expandRow(0);
    }

    public void clearTree() {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        root.removeAllChildren();
        model.reload();
    }

    public void addTemplate(DefaultMutableTreeNode node) {
        root.add(node);
    }

    public String getSelectedItem() {
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selected != null && selected != root && selected.isLeaf()) {
            return selected.toString();
        }
        return null;
    }

    public void onTemplateSelected(Runnable onTemplateSelected) {
        tree.addTreeSelectionListener(e -> onTemplateSelected.run());
    }
}
