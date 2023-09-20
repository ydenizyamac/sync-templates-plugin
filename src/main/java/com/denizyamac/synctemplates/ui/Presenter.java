package com.denizyamac.synctemplates.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Presenter {
    private final View view;
    private final String packageName, projectPath;
    private final String[] categories = {"Service", "Controller"};

    public Presenter(View view, String packageName, String projectPath) {
        this.view = view;
        this.packageName = packageName;
        this.projectPath = projectPath;
    }


    public void show() {
        createTree(new ArrayList<String>());
        view.setOnKeyPress(this::search);
        view.onTemplateSelected(this::onTemplateSelected);
        view.setOKActionEnabled(false);
        view.show();
        if (view.isOK()) {

        }
    }

    private void createTree(List<String> templates) {
        DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[categories.length];
        DefaultMutableTreeNode others = new DefaultMutableTreeNode("Other");
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new DefaultMutableTreeNode(categories[i]);
        }

        for (String templateName : templates) {
            DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(templateName);
            boolean isOther = true;
            for (int i = 0; i < nodes.length; i++) {
                if (templateName.endsWith(categories[i])) {
                    nodes[i].add(leaf);
                    isOther = false;
                    break;
                }

            }
            if (isOther) {
                others.add(leaf);
            }
        }

        for (DefaultMutableTreeNode node : nodes) {
            if (!node.isLeaf()) {
                view.addTemplate(node);
            }
        }
        if (!others.isLeaf()) {
            view.addTemplate(others);
        }
        view.expandRoot();
    }

    private String packageToPath(String packageName) {
        return packageName.replace(".", "/");
    }

    private void search() {
        view.clearTree();
        createTree(new ArrayList<String>().stream().filter(p -> p.contains(view.getSearchTextField().getText())).collect(Collectors.toList()));
    }

    private void onTemplateSelected() {
        view.setOKActionEnabled(view.getSelectedItem() != null);
    }
}
