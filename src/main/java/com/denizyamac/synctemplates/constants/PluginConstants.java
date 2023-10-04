package com.denizyamac.synctemplates.constants;

import com.denizyamac.synctemplates.config.PluginSettings;

public class PluginConstants {
    public static final String BLOB_URL = "https://raw.githubusercontent.com/ydenizyamac/idea-templates/main/";
    public static final String CONFIG_FILE_NAME = "config.json";
    public static final String PLUGIN_ACTION_GROUP = "TemplateGeneratorActionGroup";
    public static final String PLUGIN_UPDATE_TEMPLATES_ACTION = "UpdateTemplatesAction";
    public static final String PLUGIN_UPDATE_TEMPLATES_ACTION_TEXT = "Update Templates";
    public static final String PLUGIN_SEARCH_TEMPLATES_ACTION = "SearchTemplatesAction";
    public static final String PLUGIN_SEARCH_TEMPLATES_ACTION_TEXT = "Search Templates";
    public static String pluginMainGroupText = "YKT Booster";
    public static String actionIdPrefix = "com.denizyamac.synctemplates";
    public static final String ICON_FOLDER = "/icon/";
    public static final String PLUGIN_CONFIG_KEY = "syncTempPluginConfig";
    public static final String PLUGIN_TEMPLATES_KEY = "syncTempPluginTemplates";
    public static final String REPOSITORY_URL_KEY = "com.denizyamac.synctemplates.repositoryUrl";
    public static final String CONFIG_FILE_NAME_KEY = "com.denizyamac.synctemplates.configFileName";
    public static final String PLUGIN_PARENT_MENU_LIST_KEY = "com.denizyamac.synctemplates.pluginParentMenuList";

    public static class Helper {
        public static String getConfigUrl() {
            return PluginSettings.getRepositoryUrl() + PluginSettings.getConfigFileName();
        }

        public static String getTemplatesUrl(String directorshipPath, String managementPath) {
            var add = !managementPath.endsWith("/");
            if (add)
                return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath + "/" + "templates.json";
            return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath + "templates.json";
        }

        public static String getFileUrl(String directorshipPath, String managementPath, String fileName, String extension) {
            return PluginSettings.getRepositoryUrl() + directorshipPath + "/" + managementPath.replace("/", "") + "/" + fileName + "." + extension;
        }


        public static String getActionId(String id) {
            return actionIdPrefix + "." + id;
        }
    }

}
