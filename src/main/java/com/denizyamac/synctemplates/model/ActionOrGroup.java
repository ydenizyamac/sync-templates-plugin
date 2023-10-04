package com.denizyamac.synctemplates.model;

import com.denizyamac.synctemplates.constants.PluginConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ActionOrGroup {
    private String name;
    private String templateName;
    private ActionOrGroupTypeEnum type;
    private List<ActionOrGroup> children;
    private String path;
    private String[] synonyms;
    private Boolean root;
    private String management;
    private String[] managementSynonyms;


    public static ActionOrGroup create(String name, String templateName, ActionOrGroupTypeEnum type, List<ActionOrGroup> children, String path, String[] synonyms, Boolean root) {
        return new ActionOrGroup(name, templateName, type, children, path, synonyms, root);
    }


    public ActionOrGroup(String name, String templateName, ActionOrGroupTypeEnum type, List<ActionOrGroup> children, String path, String[] synonyms, Boolean root) {
        this.name = name;
        this.templateName = templateName;
        this.type = type;
        this.children = children;
        this.path = path;
        this.synonyms = synonyms;
        this.root = root;
    }

    public String getId() {
        return PluginConstants.Helper.getActionId(path.replace(" ", "").replace("/", ""));
    }

}
