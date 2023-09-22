package com.denizyamac.synctemplates.ui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class TemplateTreeModel implements TreeModel {

    private List<TreeNode> nodes;

    public TemplateTreeModel() {
        nodes = new ArrayList<>();
    }

    @Override
    public Object getRoot() {
        return nodes.get(0);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((TreeNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeNode) node).getChildCount() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // TODO: Implement this method if needed.
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeNode) parent).getIndex((TreeNode) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }

    public void addNodes(List<TreeNode> nodes) {
        this.nodes.addAll(nodes);
    }

    public void addNode(TreeNode node) {
        this.nodes.add(node);
    }
}