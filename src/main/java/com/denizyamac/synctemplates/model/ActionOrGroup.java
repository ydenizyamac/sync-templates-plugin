package com.denizyamac.synctemplates.model;

import com.denizyamac.synctemplates.constants.PluginConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActionOrGroup {
    private String name;
    private String templateName;
    private ActionOrGroupTypeEnum type;
    private List<ActionOrGroup> children;
    private String path;
    private String icon;
    private String kind;
    private String[] addInto;
    private String[] synonyms;
    private Boolean subMenu;
    private Boolean mainMenuActive;
    private Boolean root;


    public static ActionOrGroup create(String name, String templateName, ActionOrGroupTypeEnum type, List<ActionOrGroup> children, String path, String icon, String kind, String[] addInto, String[] synonyms, Boolean isSubMenu, Boolean isMainMenuActive, Boolean root) {
        return new ActionOrGroup(name, templateName, type, children, path, icon, kind, addInto, synonyms, isSubMenu, isMainMenuActive, root);
    }


    public ActionOrGroup(String name, String templateName, ActionOrGroupTypeEnum type, List<ActionOrGroup> children, String path, String icon, String kind, String[] addInto, String[] synonyms, Boolean isSubMenu, Boolean isMainMenuActive, Boolean root) {
        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.children = children;
        this.path = path;
        this.icon = icon;
        this.kind = kind;
        this.addInto = addInto;
        this.synonyms = synonyms;
        this.subMenu = isSubMenu;
        this.mainMenuActive = isMainMenuActive;
        this.root = root;
    }

    public String getId() {
        return PluginConstants.Helper.getActionId(path.replace(" ", "").replace("/", ""));
    }

}
