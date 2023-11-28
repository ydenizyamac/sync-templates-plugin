package com.denizyamac.synctemplates.helper;

import com.denizyamac.synctemplates.config.PluginSettings;
import com.denizyamac.synctemplates.constants.PluginConstants;
import com.denizyamac.synctemplates.executor.ExecutorServiceHolder;
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
import org.apache.commons.io.FilenameUtils;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateHelper {
    private static final AtomicBoolean errorThrown = new AtomicBoolean(false);

    public static void getTemplates(boolean force) {
        errorThrown.set(false);
        var directorships = TemplateHelper.getDirectorships(force);
        if (directorships != null) {
            if (Boolean.TRUE.equals(force)) {
                Template[] templates = TemplateHelper.getAllTemplates(directorships);
                if (templates != null) {
                    TemplateHelper.addAllTemplates(directorships, true, () -> {
                        GroupHelper.generateGroups(templates);
                        ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Templates updated", "Info"));
                    });
                }
            } else {
                Template[] templates = PluginSettings.getTemplates();
                if (templates == null) {
                    templates = TemplateHelper.getAllTemplates(directorships);

                }
                if (templates != null) {
                    GroupHelper.generateGroups(templates);
                    TemplateHelper.addAllTemplates(directorships, false, null);
                    //ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Templates Updated", "Info"));
                }
            }
        } else if (Boolean.FALSE.equals(errorThrown.get())) {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Please check plugin configuration", "ERROR"));
        }
    }

    private static void addAllTemplates(Directorship[] directorships, boolean forceUpdate, Runnable callback) {
        if (directorships != null) {
            var templates = PluginSettings.getTemplates();
            if (templates != null) {
                List<CompletableFuture<List<String>>> futures = new ArrayList<>();
                FileTemplateManager fileTemplateManager = FileTemplateManager.getDefaultInstance();
                FileTemplate[] fileTemplates = fileTemplateManager.getInternalTemplates();
                AtomicReference<FileTemplate[]> fileTemplatesRef = new AtomicReference<>(fileTemplates);

                for (var templateItem : templates) {
                    String[] files = templateItem.getFiles();
                    for (var file : files) {
                        String uniqueName = templateItem.getTemplateFileUniqueName(file);
                        String templateStr = PluginSettings.getTemplateContent(uniqueName);
                        if (templateStr == null || forceUpdate) {
                            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
                                var str = TemplateHelper.doCallAsync(PluginConstants.Helper.getFileUrl(templateItem.getDirectorshipPath(), templateItem.getManagementPath(), file));
                                return Arrays.asList(uniqueName, str, file);
                            }, ExecutorServiceHolder.EXECUTOR_SERVICE);
                            futures.add(future);
                            //templateStr = TemplateHelper.doCallAsync(PluginConstants.Helper.getFileUrl(templateItem.getDirectorshipPath(), templateItem.getManagementPath(), file));
                        } else {
                            PluginSettings.setTemplateContent(uniqueName, templateStr);
                            if (Arrays.stream(fileTemplates).noneMatch(p -> p.getName().equals(uniqueName))) {
                                if (!templateStr.isBlank() && !templateStr.isEmpty()) {
                                    var template = FileTemplateUtil.createTemplate(uniqueName, FilenameUtils.getExtension(file), templateStr, fileTemplates);
                                    fileTemplatesRef.set(ArrayUtil.append(fileTemplatesRef.get(), template));
                                }
                            }
                        }
                    }
                }
                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allOf.thenRun(() -> {
                    for (CompletableFuture<List<String>> future : futures) {
                        List<String> resultList = future.join();
                        var uniqueName = resultList.get(0);
                        var templateStr = resultList.get(1);
                        var file = resultList.get(2);
                        if (templateStr != null) {
                            PluginSettings.setTemplateContent(uniqueName, templateStr);
                            if (Arrays.stream(fileTemplates).noneMatch(p -> p.getName().equals(uniqueName))) {
                                if (!templateStr.isBlank() && !templateStr.isEmpty()) {
                                    var template = FileTemplateUtil.createTemplate(uniqueName, FilenameUtils.getExtension(file), templateStr, fileTemplates);
                                    fileTemplatesRef.set(ArrayUtil.append(fileTemplatesRef.get(), template));
                                }
                            }
                        }
                    }
                }).thenRun(() -> {
                    if (callback != null) {
                        callback.run();
                    }
                    fileTemplateManager.setTemplates(FileTemplateManager.INTERNAL_TEMPLATES_CATEGORY, Arrays.asList(fileTemplatesRef.get()));
                    fileTemplateManager.saveAllTemplates();
                });

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

        return templates;
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }


    // Updated: Asynchronous version of doCall
    public static String doCallAsync(String url) {
        try {
            // Simulating an asynchronous HTTP call
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> doCall(url), ExecutorServiceHolder.EXECUTOR_SERVICE);
            return future.get(); // Blocking, use callbacks or other CompletableFuture methods for non-blocking handling
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return null;
        }
    }

    public static String doCall(String url) {
        Boolean basicAuth = PluginSettings.getBasicAuthEnabled();

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER) // to get 302 from bitbucket
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        try {
            if (Boolean.FALSE.equals(errorThrown.get())) {
                HttpRequest.Builder builder = HttpRequest.newBuilder()

                        .GET()
                        .uri(URI.create(url));
                if (basicAuth) {
                    builder = builder.header("Authorization", getBasicAuthenticationHeader(PluginSettings.getUsername(), PluginSettings.getPassword()));
                }
                HttpRequest request = builder.build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == HttpStatus.SC_MOVED_TEMPORARILY || response.statusCode() == HttpStatus.SC_UNAUTHORIZED || response.statusCode() == HttpStatus.SC_FORBIDDEN) {
                    errorThrown.set(true);
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Please check configuration, repository needs authentication!", "ERROR"));
                    return null;
                } else if (response.statusCode() == HttpStatus.SC_NOT_FOUND) {
                    errorThrown.set(true);
                    ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Please check configuration, '" + url + "' is not accessible!!", "ERROR"));
                    return null;
                }
                return response.body();
            }
            return null;
        } catch (IOException | InterruptedException e) {
            errorThrown.set(true);
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog("Url: " + url + "\n" + e.getMessage(), "ERROR"));
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