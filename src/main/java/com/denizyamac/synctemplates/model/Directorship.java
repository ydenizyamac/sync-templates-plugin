package com.denizyamac.synctemplates.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Directorship {
    private String name;
    private String path;
    private Management[] managements;
}
