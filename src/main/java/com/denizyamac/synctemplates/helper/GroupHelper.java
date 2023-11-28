package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.action.SearchAction;
import com.denizyamac.synctemplates.action.UpdateTemplatesAction;
import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.model.ActionOrGroup;
import com.denizyamac.synctemplates.model.ActionOrGroupTypeEnum;
import com.denizyamac.synctemplates.model.Template;
import com.denizyamac.synctemplates.model.TemplateInput;
import com.denizyamac.synctemplates.ui.CellRenderer;
import com.denizyamac.synctemplates.ui.CreateMultipleFromTemplateDialog;
import com.denizyamac.synctemplates.ui.MultipleTemplatePopup;
import com.denizyamac.synctemplates.ui.TemplateTreeModel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.treeStructure.Tree;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.nio.file.Paths;
import java.util.*;
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
                var actionOrGroup = ActionOrGroup.create(template.getDirectorship(), "directorship_" + template.getDirectorshipPath(), ActionOrGroupTypeEnum.GROUP, new ArrayList<>(), template.getDirectorshipPath(), null, true);
                groups.add(actionOrGroup);
            }
            var directorship = groups.stream().filter(p -> p.getPath().equals(template.getDirectorshipPath())).findFirst().orElse(null);
            assert directorship != null;
            var directorshipChildren = directorship.getChildren();
            //Müdürlükler
            if (directorshipChildren.stream().noneMatch(p -> p.getPath().equals(template.getManagementPath()))) {
                var uName = directorship.getUniqueName() + "_management_" + template.getManagementPath();
                var actionOrGroup = ActionOrGroup.create(template.getManagement(), uName, ActionOrGroupTypeEnum.GROUP, new ArrayList<>(), template.getManagementPath(), template.getManagementSynonyms(), false);
                directorshipChildren.add(actionOrGroup);
            }
            for (int i = 0; i < orderedGroups.length; i++) {
                var name = orderedGroups[i];
                var type = i != orderedGroups.length - 1 ? ActionOrGroupTypeEnum.GROUP : ActionOrGroupTypeEnum.ACTION;
                var path = getPath(orderedGroups, i);

                var management = directorshipChildren.stream().filter(p -> p.getPath().equals(template.getManagementPath())).findAny().orElse(null);
                int layer = 0;
                ActionOrGroup parent = management;
                while (layer < i) {
                    String _path = getPath(orderedGroups, layer);
                    assert parent != null;
                    parent = parent.getChildren().stream().filter(p -> p.getPath().equals(_path)).findFirst().orElse(null);
                    layer++;
                }
                assert parent != null;
                if (parent.getChildren().stream().noneMatch(p -> path.equals(p.getPath()))) {
                    String uniqueName = template.getDirectorshipPath() + "_" + template.getManagementPath() + "_" + path;
                    String[] synonyms = null;
                    if (type == ActionOrGroupTypeEnum.ACTION) {
                        management.setManagement(template.getManagement());
                        management.setManagementSynonyms(template.getManagementSynonyms());//TODO:Deniz
                        synonyms = template.getSynonyms();
                        uniqueName = template.getTemplateUniqueName();
                    }
                    var actionOrGroup = ActionOrGroup.create(name, uniqueName, type, new ArrayList<>(), path, synonyms, false);

                    actionOrGroup.setFiles(template.getFiles());
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
        var pluginMenu = actionManager.getAction(PluginConstants.PLUGIN_ACTION_GROUP);
        if (pluginMenu != null) {
            var _pluginMenu = (DefaultActionGroup) pluginMenu;
            var directorshipActions = Arrays.stream(_pluginMenu.getChildActionsOrStubs()).filter(p -> actionManager.getId(p).startsWith(PluginConstants.actionIdPrefix)).collect(Collectors.toList());
            for (var parent : directorshipActions) {
                _pluginMenu.remove(parent);
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

        if (!roots.isEmpty()) {
            var menuName = PluginConstants.PLUGIN_ACTION_GROUP;
            var mainMenu = (DefaultActionGroup) actionManager.getAction(menuName);
            for (var root : roots.stream().sorted(Comparator.comparing(ActionOrGroup::getName)).collect(Collectors.toList())) {
                var action = actionManager.getAction(root.getId());
                mainMenu.add(action);

                PluginSettings.addParentMenu(root.getName());
            }
        }
    }

    public static Tree generateTree(Template[] templates) {

        var groupStructure = generateGroupStructure(templates);
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

    private static List<String> getAllJavaPackages(AnActionEvent e, String currentModule) {
        Project project = e.getProject();
        assert project != null;
        ProjectRootManager rootMan = ProjectRootManager.getInstance(project);

        List<String> packageList = new ArrayList<>();
        var contentSourceRoots = rootMan.getContentSourceRoots();
        var contentRoots = rootMan.getContentRoots();
        for (var root : contentSourceRoots) {
            String s = Paths.get(currentModule).relativize(Paths.get(root.getPath())).toString();
            if (!s.startsWith("target") && !s.startsWith("build")) {
                if (root.getPath().startsWith(currentModule) && Arrays.stream(contentRoots).noneMatch(p -> !p.getPath().equals(currentModule) && p.getPath().startsWith(currentModule) && root.getPath().startsWith(p.getPath()))) {
                    var relativePath = getRelativePath(root.getPath(), currentModule);
                    packageList.add(relativePath);
                    collectPackages(root, currentModule, packageList);
                }
            }

        }
        return packageList;
    }

    private static String getFileModulePath(Project project, String filePath) {
        ProjectRootManager rootMan = ProjectRootManager.getInstance(project);
        var mod = Arrays.stream(rootMan.getContentRoots()).sorted(Comparator.comparingInt(r -> ((VirtualFile) r).getPath().length()).reversed()).filter(p -> filePath.startsWith(p.getPath())).findFirst();
        return mod.map(VirtualFile::getPath).orElse(null);
    }

    private static void collectPackages(VirtualFile directory, String basePath, List<String> packages) {

        for (VirtualFile child : directory.getChildren()) {
            if (child.isDirectory()) {
                VfsUtilCore.visitChildrenRecursively(child, new VirtualFileVisitor<VirtualFile>() {
                    @Override
                    public boolean visitFile(@NotNull VirtualFile file) {
                        if (file.isDirectory()) {
                            String relativePath = getRelativePath(file.getPath(), basePath);
                            packages.add(relativePath);
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

    }

    private static String getRelativePath(String absolutePath, String basePath) {
        //return absolutePath.substring(basePath.length() + 1); // +1 to remove the leading slash
        return Paths.get(basePath).relativize(Paths.get(absolutePath)).toString();
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
                        presentation.setEnabled(GroupHelper.isPackage(e));
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
                action = new AnAction(item::getName, icon) {

                    @Override
                    public void update(@NotNull AnActionEvent e) {
                        Presentation presentation = e.getPresentation();
                        presentation.setEnabled(GroupHelper.isPackage(e));
                    }

                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        String currentModule = getFileModulePath(e.getProject(), getCurrentDirPath(e));
                        SwingUtilities.invokeLater(() -> MultipleTemplatePopup.showPopup(item.getName(), item.getFiles(), getCurrentDirPath(e), getAllJavaPackages(e, currentModule).stream().sorted().toArray(String[]::new), (Map<String, TemplateInput> inputs) -> {
                            Project _project = Objects.requireNonNull(e.getProject());
                            new CreateMultipleFromTemplateDialog(_project, item, inputs, currentModule).create();
                        }));

                    }
                };
                addSynonyms(action, item.getSynonyms());
                actionManager.registerAction(item.getId(), action);

            }
            return action;
        }

    }

    private static String getCurrentDirPath(AnActionEvent e) {
        Project project = e.getProject();
        Object data = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
        if (data != null) {
            data = Objects.requireNonNull(e.getData(LangDataKeys.PSI_ELEMENT_ARRAY))[0];
            assert project != null;
            VirtualFile virtualFile;
            if (data instanceof PsiDirectory) {
                virtualFile = ((PsiDirectory) data).getVirtualFile();
            } else {
                assert ((PsiElement) data).getContainingFile().getParent() != null;
                virtualFile = ((PsiElement) data).getContainingFile().getParent().getVirtualFile();
            }
            return virtualFile.getPath();
        }
        return null;
    }

    public static Boolean isPackage(AnActionEvent e) {
        try {
            Project project = e.getProject();

            Object data = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY);
            if (data != null) {
                data = Objects.requireNonNull(e.getData(LangDataKeys.PSI_ELEMENT_ARRAY))[0];
                assert project != null;
                ProjectRootManager rootMan = ProjectRootManager.getInstance(project);
                VirtualFile[] roots = rootMan.getContentSourceRoots();
                VirtualFile virtualFile;
                if (data instanceof PsiDirectory) {
                    virtualFile = ((PsiDirectory) data).getVirtualFile();
                } else {
                    virtualFile = ((PsiElement) data).getContainingFile().getVirtualFile();
                }

                return Arrays.stream(roots).anyMatch(p -> virtualFile.getPath().startsWith(p.getPath()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
