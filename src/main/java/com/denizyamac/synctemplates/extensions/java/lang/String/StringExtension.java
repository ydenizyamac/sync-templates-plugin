package com.denizyamac.synctemplates.extensions.java.lang.String;

import com.denizyamac.synctemplates.helper.JsonHelper;
import com.denizyamac.synctemplates.model.Directorship;
import com.denizyamac.synctemplates.model.Template;
//import manifold.ext.rt.api.Extension;
//import manifold.ext.rt.api.This;

//@Extension
public class StringExtension {
    public static Directorship[] toConfig(String json) {
        return JsonHelper.convertToObject(json, Directorship[].class);
    }
    public static Template[] toTemplateArray(String json) {
        return JsonHelper.convertToObject(json, Template[].class);
    }
}
