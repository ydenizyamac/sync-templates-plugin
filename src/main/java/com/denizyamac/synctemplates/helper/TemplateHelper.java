package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.model.Directorship;
import com.denizyamac.synctemplates.model.Management;
import com.denizyamac.synctemplates.model.Template;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateHelper {
    public static void getTemplates(boolean force) {
        var directorships = TemplateHelper.getDirectorships(force);
        if (directorships != null) {
            if (Boolean.TRUE.equals(force)) {
                Template[] templates = TemplateHelper.getAllTemplates(directorships);
                if (templates != null) {
                    TemplateHelper.addAllTemplates(directorships, true);
                    GroupHelper.generateGroups(templates);
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Templates updated", "Info"));
                }
            } else {
                Template[] templates = PluginSettings.getTemplates();
                if (templates == null) {
                    templates = TemplateHelper.getAllTemplates(directorships);

                }
                if (templates != null) {
                    TemplateHelper.addAllTemplates(directorships, false);
                    //ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Templates Updated", "Info"));
                }
            }
        } else
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Please check plugin configuration", "Config Error"));
    }

    private static void addAllTemplates(Directorship[] directorships, boolean forceUpdate) {
        if (directorships != null) {
            var templates = PluginSettings.getTemplates();
            if (templates != null) {
                FileTemplateManager fileTemplateManager = FileTemplateManager.getDefaultInstance();
                FileTemplate[] fileTemplates = fileTemplateManager.getInternalTemplates();
                for (var templateItem : templates) {
                    String templateName = templateItem.getTemplateName();

                    String templateExtension = templateItem.getTemplateExtension();
                    String templateStr = PluginSettings.getTemplateContent(templateName);
                    if (templateStr == null || forceUpdate) {
                        templateStr = TemplateHelper.doCall(PluginConstants.Helper.getFileUrl(templateItem.getDirectorshipPath(), templateItem.getManagementPath(), templateName, templateExtension));
                    }

                    if (templateStr != null) {
                        PluginSettings.setTemplateContent(templateName, templateStr);
                        if (Arrays.stream(fileTemplates).noneMatch(p -> p.getName().equals(templateName))) {
                            if (!templateStr.isBlank() && !templateStr.isEmpty()) {
                                var template = FileTemplateUtil.createTemplate(templateItem.getTemplateUniqueName(), templateExtension, templateStr, fileTemplates);
                                fileTemplates = ArrayUtil.append(fileTemplates, template);
                            }
                        }
                    }
                }
                fileTemplateManager.setTemplates(FileTemplateManager.INTERNAL_TEMPLATES_CATEGORY, Arrays.asList(fileTemplates));
                fileTemplateManager.saveAllTemplates();
            } else {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Could not get templates", "Warning"));
            }
        }
    }

    public static Directorship[] getDirectorships(boolean forceUpdate) {
        var directorships = PluginSettings.getConfig();
        if (directorships == null || Boolean.TRUE.equals(forceUpdate)) {
            directorships = TemplateHelper.readFromUrl(PluginConstants.Helper.getConfigUrl(), Directorship[].class);
            if (directorships != null) {
                PluginSettings.setConfig(directorships);
            }
            return directorships;
        }
        return directorships;

    }

    public static Template[] getAllTemplates(Directorship[] directorships) {
        Template[] templates;
        List<Template> templateList = new ArrayList<>();
        for (Directorship directorship : directorships) {
            Management[] managements = directorship.getManagements();
            for (var management : managements) {
                //TODO: make it async call
                templates = TemplateHelper.readFromUrl(PluginConstants.Helper.getTemplatesUrl(directorship.getPath(), management.getPath()), Template[].class);
                if (templates != null) {
                    List<Template> _templateList = Arrays.stream(templates).peek(p -> {
                        p.setDirectorship(directorship.getName());
                        p.setDirectorshipPath(directorship.getPath());
                        p.setManagement(management.getName());
                        p.setManagementPath(management.getPath());
                        p.setManagementSynonyms(management.getSynonyms());
                    }).collect(Collectors.toList());
                    templateList.addAll(_templateList);
                } else {
                    if (PluginSettings.getDebugPopupEnabled()) {
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Could not fetch templates from remote\n directorship:" + directorship.getName() + "\npath:" + directorship.getPath() + "\nmanagement:" + management.getName() + "\npath:" + management.getPath(), "Error"));
                    }
                    return null;
                }
            }
        }
        templates = templateList.toArray(Template[]::new);
        if (templates != null) {
            PluginSettings.setTemplates(templates);
        }
        GroupHelper.generateGroups(templates);
        return templates;
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public static String doCall(String url) {
        Boolean basicAuth = PluginSettings.getBasicAuthEnabled();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url));
            if (basicAuth) {
                builder = builder.header("Authorization", getBasicAuthenticationHeader(PluginSettings.getUsername(), PluginSettings.getPassword()));
            }
            HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED || response.statusCode() == HttpStatus.SC_FORBIDDEN) {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Please check configuration, repository needs authentication!", " ERROR:"));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (PluginSettings.getDebugPopupEnabled()) {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(e.getMessage(), " ERROR: \n Url: " + url + "\n"));
            }
            e.printStackTrace();
            return null;
        }

    }

    /*
        public static String readStringFromUrl(String url) {
            try {
                URL u = new URL(url);
                try (InputStream in = u.openStream()) {
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return null;
                }
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    */
    public static <T> T readFromUrl(String url, Class<T> type) {
        String str = doCall(url);
        if (str != null) {
            return JsonHelper.convertToObject(str, type);
        }
        return null;
    }

    private static String getBase64EncodedImage(String imageURL) throws IOException {
        java.net.URL url = new java.net.URL(imageURL);
        InputStream is = url.openStream();
        byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(is);
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
/*
    public static Icon getIcon(String name) {

        try {
            String icon = PluginSettings.getIcon(name);
            if (icon == null) {
                icon = getBase64EncodedImage(PluginConstants.Helper.getFileUrl(name));
                PluginSettings.addIcon(name, icon);
            }
            byte[] btDataFile = Base64.decodeBase64(icon);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(btDataFile));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    */

}