package com.denizyamac.synctemplates.config;

import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.extensions.java.lang.String.StringExtension;
import com.denizyamac.synctemplates.helper.JsonHelper;
import com.denizyamac.synctemplates.model.PluginConfig;
import com.intellij.ide.util.PropertiesComponent;
import org.bouncycastle.util.Arrays;

public class PluginSettings {


    // Get the value of a property
    public static String getRepositoryUrl() {
        return PropertiesComponent.getInstance().getValue(PluginConstants.REPOSITORY_URL_KEY);
    }

    // Set the value of a property
    public static void setRepositoryUrl(String value) {
        PropertiesComponent.getInstance().setValue(PluginConstants.REPOSITORY_URL_KEY, value);
    }

    public static String getConfigFileName() {
        return PropertiesComponent.getInstance().getValue(PluginConstants.CONFIG_FILE_NAME_KEY);
    }

    public static void setConfigFileName(String value) {
        PropertiesComponent.getInstance().setValue(PluginConstants.CONFIG_FILE_NAME_KEY, value);
    }

    public static void setParentMenus(String[] menus) {
        var menuList = String.join(",", menus);
        PropertiesComponent.getInstance().setValue(PluginConstants.PLUGIN_PARENT_MENU_LIST_KEY, menuList);
    }

    public static PluginConfig getConfig() {
        var config = PropertiesComponent.getInstance().getValue(PluginConstants.PLUGIN_CONFIG_KEY);
        if (config != null && JsonHelper.isValidJson(config)) return StringExtension.toConfig(config);
        return null;
    }

    public static void setConfig(PluginConfig config) {
        var propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(PluginConstants.PLUGIN_CONFIG_KEY, config.toString());
    }

    public static String[] getParentMenus() {
        var menuList = PropertiesComponent.getInstance().getValue(PluginConstants.PLUGIN_PARENT_MENU_LIST_KEY);
        if (menuList != null) {
            return menuList.split(",");
        }
        return null;
    }

    public static void addParentMenu(String menu) {
        var list = getParentMenus();
        if (list == null) {
            list = new String[]{menu};
        } else {
            if (java.util.Arrays.stream(list).noneMatch(p -> p.equals(menu))) {
                list = Arrays.append(list, menu);
            }
        }
        setParentMenus(list);
    }

    public static void addIcon(String name, String b64) {
        PropertiesComponent.getInstance().setValue(name, b64);
    }

    public static String getIcon(String name) {
        return PropertiesComponent.getInstance().getValue(name);
    }

}
