package com.denizyamac.synctemplates.constants;

import com.denizyamac.synctemplates.config.PluginSettings;

public class PluginConstants {
    //public static final String BLOB_URL = "https://raw.githubusercontent.com/ydenizyamac/idea-templates/main/";
    public static final String BLOB_URL = "https://sdlc.yapikredi.com.tr/bitbucket/users/u075753/repos/ykt-code-templates/raw/";
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String PLUGIN_ACTION_GROUP = "TemplateGeneratorActionGroup";
    public static final String PLUGIN_UPDATE_TEMPLATES_ACTION = "UpdateTemplatesAction";
    public static final String PLUGIN_UPDATE_TEMPLATES_ACTION_TEXT = "Update Templates";
    public static final String PLUGIN_SEARCH_TEMPLATES_ACTION = "SearchTemplatesAction";
    public static final String PLUGIN_SEARCH_TEMPLATES_ACTION_TEXT = "Search Templates";
    public static String actionIdPrefix = "com.denizyamac.yktbooster";
    public static final String ICON_FOLDER = "/icon/";
    public static final String PLUGIN_CONFIG_KEY = "syncTempPluginConfig";
    public static final String PLUGIN_TEMPLATES_KEY = "syncTempPluginTemplates";
    public static final String REPOSITORY_URL_KEY = "com.denizyamac.synctemplates.repositoryUrl";
    public static final String CONFIG_FILE_NAME_KEY = "com.denizyamac.synctemplates.configFileName";
    public static final String PLUGIN_PARENT_MENU_LIST_KEY = "com.denizyamac.synctemplates.pluginParentMenuList";
    public static final String BASIC_AUTH_ENABLED_KEY = "com.denizyamac.synctemplates.basicAuthEnabled";
    public static final String DEBUG_POPUP_ENABLED_KEY = "com.denizyamac.synctemplates.debugPopupEnabled";
    public static final String USERNAME_KEY = "com.denizyamac.synctemplates.username";
    public static final String PASSWORD_KEY = "com.denizyamac.synctemplates.password";

    public static class Helper {
        public static String getConfigUrl() {
            var add = !PluginSettings.getRepositoryUrl().endsWith("/");
            if (add)
                return PluginSettings.getRepositoryUrl() + "/" + PluginSettings.getConfigFileName();
            return PluginSettings.getRepositoryUrl() + PluginSettings.getConfigFileName();
        }

        public static String getTemplatesUrl(String directorshipPath, String managementPath) {
            var add = !managementPath.endsWith("/");
            if (add)
                return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath + "/" + "templates.json";
            return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath + "templates.json";
        }

        public static String getFileUrl(String directorshipPath, String managementPath, String file) {
            return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath.replace("/", "") + "/" + file;
        }


        public static String getActionId(String id) {
            return actionIdPrefix + "." + id;
        }
    }

}
