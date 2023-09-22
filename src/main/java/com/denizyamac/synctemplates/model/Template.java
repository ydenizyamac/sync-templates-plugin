package com.denizyamac.synctemplates.model;

import com.denizyamac.synctemplates.constants.PluginConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Template {
    private String templateName;
    private String templateExtension;
    private String group;
    private String icon;
    private String kind;
    private String[] addInto;
    private String[] synonyms;
    private Boolean subMenu;
    private Boolean mainMenuActive;
}
