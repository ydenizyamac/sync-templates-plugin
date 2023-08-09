package com.denizyamac.synctemplates.config;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
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
        return mainPanel;
    }

    @Override
    public void apply() throws ConfigurationException {
        // Save the modified settings
        String repoUrl = repoUrl_textField.getText();
        String configName = configName_textField.getText();
        PluginSettings.setRepositoryUrl(repoUrl);
        PluginSettings.setConfigFileName(configName);
    }

    @Override
    public void reset() {
        // Reset the settings to their previous state
        String repoUrl = PluginSettings.getRepositoryUrl();
        String configName = PluginSettings.getConfigFileName();
        repoUrl_textField.setText(repoUrl);
        configName_textField.setText(configName);
    }

    @Override
    public boolean isModified() {
        // Check if the settings have been modified
        String repoUrl = repoUrl_textField.getText();
        String configName = configName_textField.getText();
        return !repoUrl.equals(PluginSettings.getRepositoryUrl()) || !configName.equals(PluginSettings.getConfigFileName());
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

    }
}
