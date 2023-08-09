package com.denizyamac.synctemplates.extensions.java.lang.String;

import com.denizyamac.synctemplates.helper.JsonHelper;
import com.denizyamac.synctemplates.model.PluginConfig;
//import manifold.ext.rt.api.Extension;
//import manifold.ext.rt.api.This;

//@Extension
public class StringExtension {
    public static PluginConfig toConfig(String json) {
        return JsonHelper.convertToObject(json, PluginConfig.class);
    }
}
