package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.model.Directorship;
import com.denizyamac.synctemplates.model.Management;
import com.denizyamac.synctemplates.model.Template;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.net.util.Base64;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateHelper {
    public static void addAllTemplatesAndGroups(Directorship[] directorships) {
        if (directorships != null) {
            var templates = PluginSettings.getTemplates();
            if (templates != null) {
                FileTemplateManager fileTemplateManager = FileTemplateManager.getDefaultInstance();
                FileTemplate[] fileTemplates = fileTemplateManager.getInternalTemplates();
                for (var templateItem : templates) {
                    String templateName = templateItem.getTemplateName();
                    String templateExtension = templateItem.getTemplateExtension();
                    String templateStr = PluginSettings.getTemplateContent(templateName);
                    if (templateStr == null) {
                        templateStr = TemplateHelper.readStringFromUrl(PluginConstants.Helper.getFileUrl(templateItem.getDirectorshipPath(), templateItem.getManagementPath(), templateName, templateExtension));
                    }

                    if (templateStr != null) {
                        PluginSettings.setTemplateContent(templateName, templateStr);
                        if (Arrays.stream(fileTemplates).noneMatch(p -> p.getName().equals(templateName))) {
                            if (!templateStr.isBlank() && !templateStr.isEmpty()) {
                                var template = FileTemplateUtil.createTemplate(templateName, templateExtension, templateStr, fileTemplates);
                                fileTemplates = ArrayUtil.append(fileTemplates, template);
                            }
                        }
                    }
                }
                fileTemplateManager.setTemplates(FileTemplateManager.INTERNAL_TEMPLATES_CATEGORY, Arrays.asList(fileTemplates));
                fileTemplateManager.saveAllTemplates();
                GroupHelper.generateGroups(templates);
                /*if (SwingUtilities.isEventDispatchThread()) {
                    Messages.showInfoMessage("TemplatesUpdated", "Info");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showInfoMessage("TemplatesUpdated", "Info");
                    });
                }*/
            } else {
                SwingUtilities.invokeLater(() -> {
                    Messages.showInfoMessage("Could not get templates", "Warning");
                });
            }
        }
    }

    public static Directorship[] getDirectorships(boolean forceUpdate) {
        var directorships = PluginSettings.getConfig();
        if (directorships == null || Boolean.TRUE.equals(forceUpdate)) {
            directorships = TemplateHelper.readFromUrl(PluginConstants.Helper.getConfigUrl(), Directorship[].class);
            PluginSettings.setConfig(directorships);
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
                List<Template> _templateList = Arrays.stream(templates).peek(p -> {
                    p.setDirectorship(directorship.getName());
                    p.setDirectorshipPath(directorship.getPath());
                    p.setManagement(management.getName());
                    p.setManagementPath(management.getPath());
                    p.setManagementSynonyms(management.getSynonyms());
                }).collect(Collectors.toList());
                templateList.addAll(_templateList);
            }
        }
        templates = templateList.toArray(Template[]::new);
        PluginSettings.setTemplates(templates);
        return templates;
    }

    public static String doCall(String url) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("ERROR", e.getMessage());
            });
            e.printStackTrace();
            return null;
        }

    }

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
        return Base64.encodeBase64String(bytes);
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