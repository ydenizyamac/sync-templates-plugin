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
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.treeStructure.Tree;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
            //Direktörlükler
            if (groups.stream().noneMatch(p -> p.getPath().equals(template.getDirectorshipPath()))) {
                var actionOrGroup = ActionOrGroup.create(template.getDirectorship(), template.getDirectorship(), ActionOrGroupTypeEnum.GROUP, new ArrayList<>(), template.getDirectorshipPath(), null, true, "directorship");
                groups.add(actionOrGroup);
            }
            var directorship = groups.stream().filter(p -> p.getPath().equals(template.getDirectorshipPath())).findFirst().get();
            var directorshipChildren = directorship.getChildren();
            //Müdürlükler
            if (directorshipChildren.stream().noneMatch(p -> p.getPath().equals(template.getManagementPath()))) {
                var actionOrGroup = ActionOrGroup.create(template.getManagement(), template.getManagement(), ActionOrGroupTypeEnum.GROUP, new ArrayList<>(), template.getManagementPath(), template.getManagementSynonyms(), false, "management");
                directorshipChildren.add(actionOrGroup);
            }
            for (int i = 0; i < orderedGroups.length; i++) {
                var name = orderedGroups[i];
                var type = i != orderedGroups.length - 1 ? ActionOrGroupTypeEnum.GROUP : ActionOrGroupTypeEnum.ACTION;
                var path = getPath(orderedGroups, i);

                var management = directorshipChildren.stream().filter(p -> p.getPath().equals(template.getManagementPath())).findAny().get();
                int layer = 0;
                ActionOrGroup parent = management;
                while (layer < i) {
                    String _path = getPath(orderedGroups, layer);
                    parent = parent.getChildren().stream().filter(p -> p.getPath().equals(_path)).findFirst().get();
                    layer++;
                }
                if (parent.getChildren().stream().noneMatch(p -> path.equals(p.getPath()))) {
                    String tName = name;
                    String uniqueName = template.getDirectorship() + "_" + template.getManagement() + "_" + path;
                    String[] synonyms = null;
                    if (type == ActionOrGroupTypeEnum.ACTION) {
                        management.setManagement(template.getManagement());
                        management.setManagementSynonyms(template.getManagementSynonyms());
                        tName = template.getTemplateName();
                        synonyms = template.getSynonyms();
                        uniqueName = template.getTemplateUniqueName();
                    }
                    var actionOrGroup = ActionOrGroup.create(name, tName, type, new ArrayList<>(), path, synonyms, false, uniqueName);
                    parent.getChildren().add(actionOrGroup);
                }
            }

        }
        return groups;

    }


    private static String[] getSplittedGroups(String group) {
        return group.split("/");
    }

    public static void clean() {
        var actionManager = ActionManager.getInstance();
        var mainMenu = (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_MAIN_MENU);
        var pluginMenu = Arrays.stream(mainMenu.getChildActionsOrStubs()).filter(p -> actionManager.getId(p).equals(PluginConstants.PLUGIN_ACTION_GROUP)).findFirst();
        if (pluginMenu.isPresent()) {
            var directorshipActions = Arrays.stream(((DefaultActionGroup) pluginMenu.get()).getChildActionsOrStubs()).filter(p -> actionManager.getId(p).startsWith(PluginConstants.actionIdPrefix)).collect(Collectors.toList());
            for (var parent : directorshipActions) {
                ((DefaultActionGroup) pluginMenu.get()).remove(parent);
            }
        }

        List<String> customActionIds = actionManager.getActionIdList(PluginConstants.actionIdPrefix);
        for (var customActionId : customActionIds) {
            actionManager.unregisterAction(customActionId);
        }
    }

    private static void addSynonyms(AnAction action, String[] synonyms) {
        if (synonyms != null) {
            for (var s : synonyms) {
                action.addSynonym(() -> s);
            }
        }

    }

    private static void walkAmongChildren(List<ActionOrGroup> children, DefaultActionGroup grp) {
        var actionManager = ActionManager.getInstance();
        for (var child : children) {
            AnAction actionOrGroup = (AnAction) generateGroup(child);
            grp.add(actionOrGroup);
            if (child.getType() == ActionOrGroupTypeEnum.GROUP) {
                List<ActionOrGroup> _children = child.getChildren();
                walkAmongChildren(_children, (DefaultActionGroup) actionOrGroup);
            }
        }
    }

    private static void walkAmongChildren_tree(List<ActionOrGroup> children, DefaultMutableTreeNode grp) {
        for (var child : children) {
            DefaultMutableTreeNode childGrp = generateTreeNode(child);
            AtomicReference<Boolean> exist = new AtomicReference<>(false);
            grp.add(childGrp);
            if (child.getType() == ActionOrGroupTypeEnum.GROUP) {
                List<ActionOrGroup> _children = child.getChildren();
                walkAmongChildren_tree(_children, childGrp);
            }
        }
    }

    public static void generateGroups(Template[] templates) {
        GroupHelper.clean();
        var groupStructure = generateGroupStructure(templates);
        var actionManager = ActionManager.getInstance();
        List<ActionOrGroup> roots = new ArrayList<>();
        for (ActionOrGroup item : groupStructure) {
            if (item.getRoot()) {
                roots.add(item);
            }
            DefaultActionGroup grp = (DefaultActionGroup) generateGroup(item);

            var children = item.getChildren();
            walkAmongChildren(children, grp);
        }
        if (roots.size() > 0) {
            for (var root : roots) {
                var action = actionManager.getAction(root.getId());
                var menuName = PluginConstants.PLUGIN_ACTION_GROUP;
                var mainMenu = (DefaultActionGroup) actionManager.getAction(menuName);
                mainMenu.add(action);
                PluginSettings.addParentMenu(menuName);
            }

        }
    }

    public static Tree generateTree(Template[] templates) {
        var groupStructure = generateGroupStructure(templates);
        var actionManager = ActionManager.getInstance();
        List<ActionOrGroup> roots = new ArrayList<>();
        TemplateTreeModel treeModel = new TemplateTreeModel();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Templates");

        if (groupStructure.stream().filter(p -> p.getType() == ActionOrGroupTypeEnum.GROUP).count() > 1) {
            treeModel.addNode(root);
        }

        for (var i = 0; i < groupStructure.size(); i++) {
            var item = groupStructure.get(i);
            if (item.getType() == ActionOrGroupTypeEnum.GROUP) {
                DefaultMutableTreeNode grpNode = generateTreeNode(item);
                var children = item.getChildren();
                walkAmongChildren_tree(children, grpNode);
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

    public static Icon getIconFromResource(String name) {
        var iconPath = GroupHelper.class.getResource(PluginConstants.ICON_FOLDER + name);
        if (iconPath != null)
            return new ImageIcon(iconPath);
        return null;
    }

    public static void createMainMenu() {
        var actionManager = ActionManager.getInstance();
        DefaultActionGroup mainGroup = (DefaultActionGroup) actionManager.getAction(PluginConstants.PLUGIN_ACTION_GROUP);
        if (actionManager.getAction(PluginConstants.PLUGIN_UPDATE_TEMPLATES_ACTION) == null) {
            var updateIcon = getIconFromResource("update.png");
            UpdateTemplatesAction updateAction = new UpdateTemplatesAction(PluginConstants.PLUGIN_UPDATE_TEMPLATES_ACTION_TEXT, updateIcon);
            actionManager.registerAction(PluginConstants.PLUGIN_UPDATE_TEMPLATES_ACTION, updateAction);
            mainGroup.add(updateAction);
        }
        if (actionManager.getAction(PluginConstants.PLUGIN_SEARCH_TEMPLATES_ACTION) == null) {
            var searchIcon = getIconFromResource("search.png");
            var projectViewPopupMenuGroupName = "ProjectViewPopupMenu";
            var projectViewPopupMenuGroup = (DefaultActionGroup) actionManager.getAction(projectViewPopupMenuGroupName);
            SearchAction searchAction = new SearchAction(PluginConstants.PLUGIN_SEARCH_TEMPLATES_ACTION_TEXT, searchIcon);
            actionManager.registerAction(PluginConstants.PLUGIN_SEARCH_TEMPLATES_ACTION, searchAction);

            projectViewPopupMenuGroup.add(searchAction, new Constraints(Anchor.AFTER, "WeighingNewGroup"));
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
                group = new DefaultActionGroup(item::getName, true) {
                    @Override
                    public void update(@NotNull AnActionEvent e) {
                        Presentation presentation = e.getPresentation();
                        // Hide the action if the clicked folder is not a Java package
                        presentation.setVisible(GroupHelper.isPackage(e));
                    }
                };
                if (item.getManagementSynonyms() != null)
                    addSynonyms(group, item.getManagementSynonyms());
                actionManager.registerAction(item.getId(), group);
            }
            return group;
        } else {
            AnAction action = actionManager.getAction(item.getId());
            if (action == null) {
                //var icon = TemplateHelper.getIcon(item.getIcon());
                var icon = getIconFromResource("fileIcon.png");
                action = new DynamicCreateFileTemplateAction(item.getName(), icon) {
                    @Override
                    public void update(@NotNull AnActionEvent e) {
                        Presentation presentation = e.getPresentation();
                        presentation.setEnabledAndVisible(GroupHelper.isPackage(e));
                    }

                    @Override
                    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.@NotNull Builder builder) {
                        var kind = "Class";
                        builder.setTitle(item.getName()).addKind(kind, icon, item.getUniqueName());
                    }
                };
                addSynonyms(action, item.getSynonyms());
                actionManager.registerAction(item.getId(), action);

            }
            return action;
        }
    }

    public static Boolean isPackage(AnActionEvent e) {
        try {
            Project project = e.getProject();
            Object data = e.getData(LangDataKeys.PSI_ELEMENT);
            if (data instanceof PsiDirectory) {
                PsiDirectory directory = (PsiDirectory) e.getData(LangDataKeys.PSI_ELEMENT);
                if (project == null || directory == null) return false;
                // Get the VirtualFile object for the selected directory
                VirtualFile virtualFile = directory.getVirtualFile();
                String c = ProjectFileIndex.getInstance(project).getPackageNameByDirectory(virtualFile);
                return StringUtil.isNotEmpty(c);
            } else {
                data = e.getData(LangDataKeys.VIRTUAL_FILE);
                if (data != null) {
                    String c = ProjectFileIndex.getInstance(project).getPackageNameByDirectory(((VirtualFile) data).getParent());
                    return StringUtil.isNotEmpty(c);
                }
            }

        } catch (Exception ignored) {
        }
        return false;
    }
}
