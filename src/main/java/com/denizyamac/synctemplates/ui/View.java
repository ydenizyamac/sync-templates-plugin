package com.denizyamac.synctemplates.ui;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.helper.GroupHelper;
import com.denizyamac.synctemplates.helper.TemplateHelper;
import com.denizyamac.synctemplates.model.ActionOrGroup;
import com.denizyamac.synctemplates.model.Template;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class View extends DialogWrapper {
    private JTextField modelNameTextField, searchTextField;
    @Getter(AccessLevel.PRIVATE)
    private JScrollPane scrollPane;
    @Getter(AccessLevel.PRIVATE)
    private Tree tree;
    @Setter
    private DataContext dataContext;
    private final int keyTreshold = 3;

    public View(AnActionEvent e) {
        super(true);
        setTitle("Templates");
        setSize(650, 500);
        init();
        //Project openProject = ProjectManager.getInstance().getOpenProjects()[0];
        //Project project = (Project) DataManager.getInstance().getDataContext().getData(LangDataKeys.PROJECT);
        //JComponent c = FileEditorManager.getInstance(openProject).getSelectedEditor().getComponent();

        setDataContext(e.getDataContext());
        //IdeFocusManager.getInstance(openProject).requestFocus(c, true);
        /*DataManager.getInstance().getDataContextFromFocusAsync().then(p -> {
            final IdeView view = LangDataKeys.IDE_VIEW.getData(p);
            if (view != null) {
                setDataContext(p);
            }
            return p;
        });*/

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
                if (searchTextField.getText().length() > keyTreshold - 1 || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && searchTextField.getText().length() == keyTreshold - 1)) {
                    runnable.run();
                }
            }
        });
    }

    private void expandAll(Tree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(Tree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                TreeNode child = node.getChildAt(i);
                TreePath path = parent.pathByAddingChild(child);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse should happen bottom-up to avoid concurrent modification issues
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        JPanel jPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        jPanel.setLayout(gridBagLayout);
        GridBagConstraints c = new GridBagConstraints();


        scrollPane = new JBScrollPane();
        scrollPane.setBounds(0, 120, 300, 300);

        JLabel searchLabel = new JLabel("Search  ");
        searchLabel.setIcon(getIconFromResource("search.png"));
        //searchLabel.setSize(300, 40);
        //noinspection MagicConstant
        searchLabel.setFont(searchLabel.getFont().deriveFont(20));

        searchLabel.setBounds(0, 0, 300, 50);
        searchLabel.setHorizontalTextPosition(SwingConstants.LEFT);

        searchTextField = new JTextField();
        searchTextField.setBounds(0, 60, 300, 40);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        jPanel.add(searchLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 0;
        jPanel.add(searchTextField, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.weighty = 1;
        scrollPane.setMinimumSize(new Dimension(300, 300));
        jPanel.add(scrollPane, c);
        // Set the minimum size of the JPanel.
        jPanel.setMinimumSize(new Dimension(300, 420));
        // Set the preferred size of the JPanel.
        jPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Set the horizontal alignment of the JPanel content to CENTER.
        jPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setOKActionEnabled(false);


        return jPanel;
    }

    private Icon getIconFromResource(String name) {
        var iconPath = View.class.getResource(PluginConstants.ICON_FOLDER + name);
        if (iconPath != null)
            return new ImageIcon(iconPath);
        return null;
    }
    /*public void clearTree() {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        root.removeAllChildren();
        model.reload();
    }*/

    public String getSelectedItem() {
        if (tree != null) {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selected != null && selected.isLeaf()) {
                Object userObject = selected.getUserObject();
                if (userObject instanceof ActionOrGroup) {
                    //@SuppressWarnings("unchecked")
                    ActionOrGroup entry = (ActionOrGroup) userObject;
                    return entry.getId();
                }
            }
        }
        return null;
    }

    void setTree(Template[] templates) {
        tree = GroupHelper.generateTree(templates);
        tree.addTreeSelectionListener(e -> onTemplateSelected());
        getScrollPane().setViewportView(tree);
    }


    @SneakyThrows
    @Override
    protected void doOKAction() {

        if (getOKAction().isEnabled()) {

            //Template template = Arrays.stream(PluginSettings.getConfig().getTemplateList()).filter(p -> p.getGroup().endsWith(getSelectedItem())).collect(Collectors.toList()).get(0);

            AnAction action = ActionManager.getInstance().getAction(getSelectedItem());
            // Create a mock input event
            InputEvent inputEvent = new KeyEvent(new JPanel(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
            // Create a mock data context
            /*IdeView view = new IdeView() {
                @Override
                public PsiDirectory @NotNull [] getDirectories() {
                    return new PsiDirectory[0];
                }

                @Override
                public @Nullable PsiDirectory getOrChooseDirectory() {
                    return null;
                }
            }*/

            AnActionEvent actionEvent = AnActionEvent.createFromAnAction(action, inputEvent, "SomePlace", dataContext);
            action.actionPerformed(actionEvent);
        }
        super.doOKAction();
    }

    @Override
    public void show() {
        TemplateHelper.getTemplates(false);
        Template[] templates = PluginSettings.getTemplates();
        setTree(templates);
        setOnKeyPress(this::search);
        super.show();
    }

    private void search() {
        //clearTree();
        String text = getSearchTextField().getText();
        Template[] templates = Arrays.stream(Objects.requireNonNull(PluginSettings.getTemplates())).filter(
                p -> StringUtil.containsIgnoreCase(p.getGroup(), text) ||
                        (p.getSynonyms() != null && Arrays.stream(p.getSynonyms()).anyMatch(pp -> StringUtil.startsWithIgnoreCase(pp, text))) ||
                        (p.getManagementSynonyms() != null && Arrays.stream(p.getManagementSynonyms()).anyMatch(pp -> StringUtil.startsWithIgnoreCase(pp, text)))).toArray(Template[]::new);
        setTree(templates);
        expandAll(tree, text.length() >= keyTreshold);


    }


    private void onTemplateSelected() {
        setOKActionEnabled(getSelectedItem() != null);
    }
}
