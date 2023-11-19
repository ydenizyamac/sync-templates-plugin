package com.denizyamac.synctemplates.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TemplateInput {
    private String tName;
    private String tPackage;
    private Boolean tInclude;
}