package com.denizyamac.synctemplates.config;

import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.extensions.java.lang.String.StringExtension;
import com.denizyamac.synctemplates.helper.JsonHelper;
import com.denizyamac.synctemplates.model.Directorship;
import com.denizyamac.synctemplates.model.Template;
import com.denizyamac.synctemplates.service.PasswordService;
import com.intellij.ide.util.PropertiesComponent;
import org.bouncycastle.util.Arrays;

public class PluginSettings {
    public static void clean() {
        //PropertiesComponent.getInstance().unsetValue(PluginConstants.BASIC_AUTH_ENABLED_KEY);
        //PropertiesComponent.getInstance().unsetValue(PluginConstants.USERNAME_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.PASSWORD_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.REPOSITORY_URL_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.CONFIG_FILE_NAME_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.PLUGIN_PARENT_MENU_LIST_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.PLUGIN_CONFIG_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.PLUGIN_TEMPLATES_KEY);
        PropertiesComponent.getInstance().unsetValue(PluginConstants.DEBUG_POPUP_ENABLED_KEY);
        PasswordService.getInstance().savePassword(null);
    }

    public static Boolean getBasicAuthEnabled() {
        return Boolean.parseBoolean(PropertiesComponent.getInstance().getValue(PluginConstants.BASIC_AUTH_ENABLED_KEY));
    }

    public static String getUsername() {
        return PropertiesComponent.getInstance().getValue(PluginConstants.USERNAME_KEY);
    }

    public static String getPassword() {
        return PasswordService.getInstance().loadPassword();
        //return PropertiesComponent.getInstance().getValue(PluginConstants.PASSWORD_KEY);
    }

    // Set the value of a property
    public static void setBasicAuthEnabled(Boolean value) {
        PropertiesComponent.getInstance().setValue(PluginConstants.BASIC_AUTH_ENABLED_KEY, String.valueOf(value));
    }  // Set the value of a property

    public static void setUsername(String value) {
        PropertiesComponent.getInstance().setValue(PluginConstants.USERNAME_KEY, value);
    }  // Set the value of a property

    public static void setPassword(String value) {
        PasswordService.getInstance().savePassword(value);
        //PropertiesComponent.getInstance().setValue(PluginConstants.PASSWORD_KEY, value);
    }

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

    public static Directorship[] getConfig() {
        var config = PropertiesComponent.getInstance().getValue(PluginConstants.PLUGIN_CONFIG_KEY);
        if (config != null && JsonHelper.isValidJson(config)) return StringExtension.toConfig(config);
        return null;
    }

    public static void setConfig(Directorship[] config) {
        var propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(PluginConstants.PLUGIN_CONFIG_KEY, JsonHelper.toString(config));
    }

    //TODO: optimize config and templates to store
    public static Template[] getTemplates() {
        var templates = PropertiesComponent.getInstance().getValue(PluginConstants.PLUGIN_TEMPLATES_KEY);
        if (templates != null && JsonHelper.isValidJson(templates)) return StringExtension.toTemplateArray(templates);
        return null;
    }

    public static void setTemplates(Template[] templates) {
        var propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(PluginConstants.PLUGIN_TEMPLATES_KEY, JsonHelper.toString(templates));
    }

    public static void setTemplateContent(String name, String content) {
        var propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.setValue(name, content);
    }

    public static String getTemplateContent(String name) {
        var propertiesComponent = PropertiesComponent.getInstance();
        return propertiesComponent.getValue(name);
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

    public static void setDebugPopupEnabled(Boolean value) {
        PropertiesComponent.getInstance().setValue(PluginConstants.DEBUG_POPUP_ENABLED_KEY, String.valueOf(value));
    }

    public static Boolean getDebugPopupEnabled() {
        return Boolean.parseBoolean(PropertiesComponent.getInstance().getValue(PluginConstants.DEBUG_POPUP_ENABLED_KEY));
    }

    public static void addIcon(String name, String b64) {
        PropertiesComponent.getInstance().setValue(name, b64);
    }

    public static String getIcon(String name) {
        return PropertiesComponent.getInstance().getValue(name);
    }

}
