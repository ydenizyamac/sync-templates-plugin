package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.action.DynamicCreateFileTemplateAction;
import com.denizyamac.synctemplates.action.SearchAction;
import com.denizyamac.synctemplates.action.UpdateTemplatesAction;
import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.model.ActionOrGroup;
import com.denizyamac.synctemplates.model.ActionOrGroupTypeEnum;
import com.denizyamac.synctemplates.model.Template;
import com.denizyamac.synctemplates.ui.CellRenderer;
import com.denizyamac.synctemplates.ui.TemplateTreeModel;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GroupHelper {
    private static String getPath(String[] groups, Integer index) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < index + 1; i++) {
            String grp = groups[i].replace(" ", "");
            if (i < index) {
                grp += "/";
            }
            builder.append(grp);
        }
        return builder.toString();
    }

    private static List<ActionOrGroup> generateGroupStructure(Template[] templates) {
        List<ActionOrGroup> groups = new ArrayList<>();
        for (Template template : templates) {
            String[] orderedGroups = getSplittedGroups(template.getGroup());
            for (int i = 0; i < orderedGroups.length; i++) {
                var name = orderedGroups[i];
                var type = i != orderedGroups.length - 1 ? ActionOrGroupTypeEnum.GROUP : ActionOrGroupTypeEnum.ACTION;
                var root = i == 0;
                var path = getPath(orderedGroups, i);
                List<ActionOrGroup> children = new ArrayList<>();
                if (groups.stream().noneMatch(p -> p.getPath().equals(path))) {
                    if (i > 0 && groups.size() >= i) {
                        var parentOpt = groups.stream().filter(p -> path.equals(p.getPath() + "/" + name.replace(" ", ""))).findAny();
                        if (parentOpt.isPresent()) {
                            var parent = parentOpt.get();

                            if (parent.getChildren().stream().noneMatch(p -> p.getPath().equals(path))) {

                                var actionOrGroup = ActionOrGroup.create(name, template.getTemplateName(), type, children, path, template.getIcon(), template.getKind(), template.getAddInto(), template.getSynonyms(), template.getSubMenu(), template.getMainMenuActive(), root);
                                parent.getChildren().add(actionOrGroup);
                                groups.add(actionOrGroup);
                            }
                        }
                    } else {
                        var actionOrGroup = ActionOrGroup.create(name, template.getTemplateName(), type, children, path, template.getIcon(), template.getKind(), template.getAddInto(), template.getSynonyms(), template.getSubMenu(), template.getMainMenuActive(), root);
                        groups.add(actionOrGroup);
                    }
                }
            }

        }
        return groups;

    }


    private static String[] getSplittedGroups(String group) {
        return group.split("/");
    }

    public static void clean() {
        var parents = PluginSettings.getParentMenus();
        if (parents != null) {
            var actionManager = ActionManager.getInstance();
            for (var parent : parents) {
                var menu = (DefaultActionGroup) actionManager.getAction(parent);
                Arrays.stream(menu.getChildren(null)).forEach(p -> {
                    var id = actionManager.getId(p);
                    if (id == null) {
                        menu.remove(p);
                        //p = null;
                    } else if (id.startsWith(PluginConstants.actionIdPrefix)) {
                        menu.remove(p);
                        actionManager.unregisterAction(id);
                    }
                });
            }
        }
    }

    private static void addSynonyms(AnAction action, String[] synonyms) {
        if (synonyms != null) {
            for (var s : synonyms) {
                action.addSynonym(() -> s);
            }
        }

    }

    public static void generateGroups(Template[] templates) {
        GroupHelper.clean();
        var groupStructure = generateGroupStructure(templates);
        var actionManager = ActionManager.getInstance();
        List<ActionOrGroup> roots = new ArrayList<ActionOrGroup>();
        for (var i = 0; i < groupStructure.size(); i++) {
            var item = groupStructure.get(i);
            if (item.getRoot()) {
                roots.add(item);
            }
            if (item.getType() == ActionOrGroupTypeEnum.GROUP) {
                DefaultActionGroup grp = (DefaultActionGroup) generateGroup(item);
                var children = item.getChildren();
                for (var child : children) {
                    AnAction childGrp = (AnAction) generateGroup(child);
                    grp.add(childGrp);
                }
            }
        }
        if (roots.size() > 0) {
            for (var root : roots) {
                var action = actionManager.getAction(root.getId());
                if (root.getMainMenuActive()) {
                    var menuName = ActionPlaces.MAIN_MENU;
                    if (root.getSubMenu()) menuName = PluginConstants.PLUGIN_ACTION_GROUP;
                    var mainMenu = (DefaultActionGroup) actionManager.getAction(menuName);
            /*    var mChildren = mainMenu.getChildren(null);
                for (var mChild : mChildren) {
                    if (actionManager.getId(mChild) == null) {
                        mainMenu.remove(mChild);
                    }
                }
             */
                    mainMenu.add(action);
                    PluginSettings.addParentMenu(menuName);
                }
                var inToL = root.getAddInto();
                for (var to : inToL) {
                    if (!to.equals(ActionPlaces.MAIN_MENU)) {
                        var m = (DefaultActionGroup) actionManager.getAction(to);
                        m.add(action);
                        PluginSettings.addParentMenu(to);
                    }
                }
            }

        }
    }

    public static Tree generateTree(Template[] templates) {
        var groupStructure = generateGroupStructure(templates);
        var actionManager = ActionManager.getInstance();
        List<ActionOrGroup> roots = new ArrayList<ActionOrGroup>();
        TemplateTreeModel treeModel = new TemplateTreeModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Templates");

        if (groupStructure.stream().filter(p -> p.getType() == ActionOrGroupTypeEnum.GROUP).count() > 1) {
            treeModel.addNode(root);
        }
        for (var i = 0; i < groupStructure.size(); i++) {
            var item = groupStructure.get(i);
            if (item.getType() == ActionOrGroupTypeEnum.GROUP) {

                //DefaultActionGroup grp = (DefaultActionGroup) actionManager.getAction(item.getId());

                //AbstractMap.SimpleEntry<String, DefaultActionGroup> entry = new AbstractMap.SimpleEntry<>(item.getName(), grp);
                DefaultMutableTreeNode grpNode = generateTreeNode(item);
                var children = item.getChildren();
                for (var child : children) {
                    //AnAction childGrp = (AnAction) actionManager.getAction(child.getId());
                    //AbstractMap.SimpleEntry<String, AnAction> childEntry = new AbstractMap.SimpleEntry<>(child.getName(), childGrp);
                    grpNode.add(generateTreeNode(child));
                }
                if (groupStructure.stream().filter(p -> p.getType() == ActionOrGroupTypeEnum.GROUP).count() > 1) {

                    root.add(grpNode);
                } else {
                    treeModel.addNode(grpNode);
                }

            }
        }
        Tree tree = groupStructure.size() > 0 ? new Tree(treeModel) : new Tree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new CellRenderer());
        return tree;
    }

    private static Icon getIconFromResource(String name) {
        var iconPath = GroupHelper.class.getResource(PluginConstants.ICON_FOLDER + name);
        return new ImageIcon(iconPath);
    }

    public static void createMainMenu() {
        var actionManager = ActionManager.getInstance();
        var mainMenu = (DefaultActionGroup) actionManager.getAction(ActionPlaces.MAIN_MENU);
        if (Arrays.stream(mainMenu.getChildren(null)).filter(Objects::nonNull).noneMatch(p -> actionManager.getId(p).equals(PluginConstants.PLUGIN_ACTION_GROUP))) {
            DefaultActionGroup mainGroup = DefaultActionGroup.createPopupGroup(() -> PluginConstants.pluginUpdateTemplatesActionText);
            actionManager.registerAction(PluginConstants.PLUGIN_ACTION_GROUP, mainGroup);

            mainMenu.add(mainGroup);
            var updateIcon = getIconFromResource("update.png");
            UpdateTemplatesAction updateAction = new UpdateTemplatesAction(PluginConstants.PLUGIN_UPDATE_TEMPLATES_ACTION_TEXT, updateIcon);
            actionManager.registerAction(PluginConstants.PLUGIN_UPDATE_TEMPLATES_ACTION, updateAction);
            mainGroup.add(updateAction);

            var searchIcon = getIconFromResource("search.png");
            var cutCopyPasteGroupName = "CutCopyPasteGroup";
            var cutCopyPasteGroup = (DefaultActionGroup) actionManager.getAction(cutCopyPasteGroupName);

            SearchAction searchAction = new SearchAction(PluginConstants.PLUGIN_SEARCH_TEMPLATES_ACTION_TEXT, searchIcon);
            actionManager.registerAction(PluginConstants.PLUGIN_SEARCH_TEMPLATES_ACTION, searchAction);
            cutCopyPasteGroup.add(searchAction);
            mainGroup.add(searchAction);
        }

    }

    private static DefaultMutableTreeNode generateTreeNode(Object item) {
        return new DefaultMutableTreeNode(item);
    }

    private static Object generateGroup(ActionOrGroup item) {
        var actionManager = ActionManager.getInstance();
        if (item.getType() == ActionOrGroupTypeEnum.GROUP) {
            DefaultActionGroup group = (DefaultActionGroup) actionManager.getAction(item.getId());
            if (group == null) {
                group = DefaultActionGroup.createPopupGroup(item::getName);
                addSynonyms(group, item.getSynonyms());
                actionManager.registerAction(item.getId(), group);
            }
            return group;
        } else {
            AnAction action = actionManager.getAction(item.getId());
            if (action == null) {
                var icon = TemplateHelper.getIcon(item.getIcon());
                action = new DynamicCreateFileTemplateAction(item.getName(), icon) {
                    @Override
                    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
                        var kind = item.getKind();
                        if (kind == null) kind = "File";
                        builder.setTitle(item.getName()).addKind(kind, icon, item.getTemplateName());
                    }
                };
                addSynonyms(action, item.getSynonyms());
                actionManager.registerAction(item.getId(), action);

            }
            return action;
        }
    }
}
