package com.denizyamac.synctemplates.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Template {
    private String templateName;
    private String templateExtension;
    private String group;
    private String[] synonyms;
    private String directorship;
    private String directorshipPath;
    private String management;
    private String managementPath;
    private String[] managementSynonyms;
}
