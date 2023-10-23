package com.denizyamac.synctemplates.config;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PluginConfigurable extends BaseConfigurable {
    // UI components
    private JPanel mainPanel;
    private JLabel repoUrl_label;
    private JTextField repoUrl_textField;
    private JLabel configName_label;
    private JTextField configName_textField;
    private JLabel basicAuth_label;
    private JCheckBox basicAuth_checkbox;
    private JLabel username_label;
    private JTextField username_textField;
    private JLabel password_label;
    private JPasswordField password_passwordField;
    private JLabel debugPopup_enabled_label;
    private JCheckBox debugPopup_enabled_checkbox;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Sync Templates";
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel();
        repoUrl_label = new JLabel("Repository URL");
        configName_label = new JLabel("Config File Name");
        repoUrl_textField = new JTextField();
        configName_textField = new JTextField();
        basicAuth_label = new JLabel("Basic Auth");
        basicAuth_checkbox = new JCheckBox();
        username_label = new JLabel("Username");
        username_textField = new JTextField();
        password_label = new JLabel("Password");
        password_passwordField = new JPasswordField();
        password_passwordField.setEchoChar('*');
        username_textField.setEnabled(false);
        password_passwordField.setEnabled(false);
        basicAuth_checkbox.addChangeListener(e -> {
            JCheckBox b = (JCheckBox) e.getSource();
            username_textField.setEnabled(b.isSelected());
            password_passwordField.setEnabled(b.isSelected());
        });
        debugPopup_enabled_label = new JLabel("Debug Popup Enabled");
        debugPopup_enabled_checkbox = new JCheckBox();
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        // Add label and text box to first row
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;

        mainPanel.add(repoUrl_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        mainPanel.add(repoUrl_textField, constraints);

        // Add label and text box to second row
        constraints.gridx = 0;
        constraints.gridy = 1;
        mainPanel.add(configName_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        mainPanel.add(configName_textField, constraints);


        // Add label and text box to second row
        constraints.gridx = 0;
        constraints.gridy = 2;
        mainPanel.add(basicAuth_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        mainPanel.add(basicAuth_checkbox, constraints);


        // Add label and text box to second row
        constraints.gridx = 0;
        constraints.gridy = 3;
        mainPanel.add(username_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        mainPanel.add(username_textField, constraints);


        // Add label and text box to second row
        constraints.gridx = 0;
        constraints.gridy = 4;
        mainPanel.add(password_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        mainPanel.add(password_passwordField, constraints);


        // Add label and text box to second row
        constraints.gridx = 0;
        constraints.gridy = 5;
        mainPanel.add(debugPopup_enabled_label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 5;
        mainPanel.add(debugPopup_enabled_checkbox, constraints);
        return mainPanel;
    }

    @Override
    public void apply() {
        // Save the modified settings
        String repoUrl = repoUrl_textField.getText();
        String configName = configName_textField.getText();
        PluginSettings.setRepositoryUrl(repoUrl);
        PluginSettings.setConfigFileName(configName);
        PluginSettings.setBasicAuthEnabled(basicAuth_checkbox.isSelected());
        PluginSettings.setUsername(username_textField.getText());
        PluginSettings.setPassword(new String(password_passwordField.getPassword()));
        PluginSettings.setDebugPopupEnabled(debugPopup_enabled_checkbox.isSelected());
    }

    @Override
    public void reset() {
        // Reset the settings to their previous state
        String repoUrl = PluginSettings.getRepositoryUrl();
        String configName = PluginSettings.getConfigFileName();
        Boolean basicAuthEnabled = PluginSettings.getBasicAuthEnabled();
        Boolean debugPopupEnabled = PluginSettings.getDebugPopupEnabled();
        String username = PluginSettings.getUsername();
        String password = PluginSettings.getPassword();
        repoUrl_textField.setText(repoUrl);
        configName_textField.setText(configName);
        basicAuth_checkbox.setSelected(basicAuthEnabled);
        username_textField.setText(username);
        password_passwordField.setText(password);
        debugPopup_enabled_checkbox.setSelected(debugPopupEnabled);
    }

    @Override
    public boolean isModified() {
        // Check if the settings have been modified
        String repoUrl = repoUrl_textField.getText();
        String configName = configName_textField.getText();
        Boolean basicAuthEnabled = basicAuth_checkbox.isSelected();
        Boolean basicAuthEnabled_state = PluginSettings.getBasicAuthEnabled();
        Boolean debugPopupEnabled = debugPopup_enabled_checkbox.isSelected();
        Boolean debugPopupEnabled_state = PluginSettings.getDebugPopupEnabled();

        String username = username_textField.getText();
        String password = new String(password_passwordField.getPassword());
        return !repoUrl.equals(PluginSettings.getRepositoryUrl()) || !configName.equals(PluginSettings.getConfigFileName()) ||
                !basicAuthEnabled.equals(basicAuthEnabled_state) || !username.equals(PluginSettings.getUsername()) ||
                !password.equals(PluginSettings.getPassword()) || !debugPopupEnabled.equals(debugPopupEnabled_state);
    }

    @Override
    public void disposeUIResources() {
        super.disposeUIResources();
        // Dispose of UI resources
        mainPanel = null;
        repoUrl_label = null;
        repoUrl_textField = null;
        configName_label = null;
        configName_textField = null;
        basicAuth_label = null;
        basicAuth_checkbox = null;
        username_label = null;
        username_textField = null;
        password_label = null;
        password_passwordField = null;
        debugPopup_enabled_label = null;
        debugPopup_enabled_checkbox = null;
    }
}
