package com.denizyamac.synctemplates.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Template {
    private String[] files;
    private String group;
    private String[] synonyms;
    private String directorship;
    private String directorshipPath;
    private String management;
    private String managementPath;
    private String[] managementSynonyms;

    @JsonIgnore
    public String getTemplateUniqueName() {
        return directorshipPath + managementPath + group.replace("/", "").replace(" ", "");
    }

    @JsonIgnore
    public String getTemplateFileUniqueName(String file) {
        return directorshipPath + managementPath + group.replace("/", "").replace(" ", "") + file.replace("/", "");
    }
}
