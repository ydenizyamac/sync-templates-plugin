package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.model.PluginConfig;
import com.denizyamac.synctemplates.model.Template;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
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
import java.util.Arrays;

public class TemplateHelper {
    public static void addAllTemplatesAndGroups(PluginConfig pluginConfig) {
        if (pluginConfig != null) {
            Template[] templates = pluginConfig.getTemplateList();
            PluginConstants.Helper.setPluginConstants(pluginConfig);
            FileTemplateManager fileTemplateManager = FileTemplateManager.getDefaultInstance();
            //List<FileTemplate> fileTemplates = new ArrayList<FileTemplate>();
            FileTemplate[] fileTemplates = fileTemplateManager.getInternalTemplates();
            if (templates != null) {
                for (var templateItem : templates) {
                    String templateName = templateItem.getTemplateName();
                    String templateExtension = templateItem.getTemplateExtension();

                    String templateStr = TemplateHelper.readStringFromUrl(PluginConstants.Helper.getFileUrl(templateName) + "." + templateExtension);

                    if (Arrays.stream(fileTemplates).noneMatch(p -> p.getName().equals(templateName))) {
                        if (templateStr != null && !templateStr.isBlank() && !templateStr.isEmpty()) {
                            var template = FileTemplateUtil.createTemplate(templateName, templateExtension, templateStr, fileTemplates);

                            //FileTemplateConfigurable configurable = new FileTemplateConfigurable(project);
                            //configurable.setProportion(0.6f);
                            //configurable.setTemplate(template, FileTemplateManagerImpl.getInstanceImpl(project).getDefaultTemplateDescription());
                            //fileTemplates.add(template);
                            //var template = fileTemplateManager.addTemplate(templateName, templateExtension);
                            //template.setText(templateStr);
                            //template.setExtension(templateExtension);
                            //template.setFileName("${NAME}");
                            //fileTemplates.add(template);
                            fileTemplates = ArrayUtil.append(fileTemplates, template);
                        }
                    }
                }
                fileTemplateManager.setTemplates(FileTemplateManager.INTERNAL_TEMPLATES_CATEGORY, Arrays.asList(fileTemplates));
                fileTemplateManager.saveAllTemplates();
                GroupHelper.generateGroups(templates);
                if (SwingUtilities.isEventDispatchThread()) {
                    Messages.showInfoMessage("TemplatesUpdated", "Info");
                } else {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showInfoMessage("TemplatesUpdated", "Info");
                    });
                }
            }


        }
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

    /* public static String getTextFromURL(String urlString) {
         try {
             // Create a URL object
             URL url = new URL(urlString);

             // Open a connection to the URL
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();

             // Set request method
             connection.setRequestMethod("GET");
             connection.setUseCaches(false);
             connection.setRequestProperty("Cache-Control", "no-cache");
             connection.setRequestProperty("Connection", "close");

             // Get the response code
             int responseCode = connection.getResponseCode();

             // If the response code indicates a successful connection (e.g., 200 for HTTP_OK)
             if (responseCode == HttpURLConnection.HTTP_OK) {
                 // Create a BufferedReader to read the response
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                 StringBuilder stringBuilder = new StringBuilder();
                 String line;

                 // Read each line of the response and append it to the StringBuilder
                 while ((line = reader.readLine()) != null) {
                     stringBuilder.append(line);
                     stringBuilder.append(System.lineSeparator());
                 }

                 // Close the reader
                 reader.close();

                 // Return the text content
                 return stringBuilder.toString();
             } else {
                 // Handle the error response if needed
                 System.out.println("HTTP error code: " + responseCode);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }

         return null;
     }
 */
    public static <T> T readFromUrl(String url, Class<T> type) {
        String str = doCall(url);
        if (str != null) {
            return JsonHelper.convertToObject(str, type);
        }
        return null;
    }


    public static Icon getIcon(String name) {

        try {
            URL url = new URL(PluginConstants.Helper.getFileUrl(name));
            Image image = ImageIO.read(url);
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}