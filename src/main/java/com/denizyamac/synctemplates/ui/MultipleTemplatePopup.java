package com.denizyamac.synctemplates.ui;

import com.denizyamac.synctemplates.model.TemplateInput;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class MultipleTemplatePopup {
    public static void showPopup(String label, String[] files, String currentDirPath, String[] options, Consumer<Map<String, TemplateInput>> action) {
        // Using a callback to retrieve selected values
        Path crDir = Paths.get(currentDirPath);
        var ff = Arrays.stream(options).filter(p -> crDir.endsWith(Paths.get(p))).findFirst();
        Callback callback = ff.map(s -> new Callback(files, s)).orElseGet(Callback::new);

        //CountDownLatch latch = new CountDownLatch(1);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = JBUI.insets(5);

        List<JPanel> inputPanels = createInputs(files, options, callback);

        for (JPanel inputPanel : inputPanels) {
            panel.add(inputPanel, gbc);
            gbc.gridy++;
        }
        JPanel commandPanel = new JPanel(new GridBagLayout());
        GridBagConstraints _gbc = new GridBagConstraints();
        _gbc.gridx = 0;
        _gbc.gridy = 0;
        _gbc.weightx = 1;
        _gbc.fill = GridBagConstraints.HORIZONTAL;
        _gbc.anchor = GridBagConstraints.EAST;
        _gbc.insets = JBUI.insets(5);
        final JButton okButton = new JButton("OK");
        okButton.setMnemonic('o');
        commandPanel.add(okButton, _gbc);
        panel.add(commandPanel, gbc);
        gbc.gridy++;
        // Place the main panel inside a JScrollPane
        JBScrollPane scrollPane = new JBScrollPane(panel) {
            @Override
            public Dimension getPreferredSize() {
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                var longest = Arrays.stream(options).max(Comparator.comparing(String::length)).get();
                var temp = new ComboBox<>();
                FontMetrics fontMetrics = temp.getFontMetrics(temp.getFont());
                return new Dimension(fontMetrics.stringWidth(longest) + 200, super.getPreferredSize().height);
            }
        };
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, inputPanels.get(0))
                .setTitle(label)
                .setResizable(true)
                .setModalContext(true)
                .setRequestFocus(true)
                .setMovable(true)
                //.setCommandButton(activeComponent)
                .createPopup();

        okButton.addActionListener((event) -> {
            //    latch.countDown();
            popup.dispose();
            action.accept(callback.getSelectedValues());
        });
        // Show the popup
        popup.showInCenterOf(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow());

        if (scrollPane.getSize().height > 700) {
            scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 700));
        }
        popup.pack(true, true);
    }


    private static List<JPanel> createInputs(String[] files, String[] options, Callback callback) {
        List<JPanel> inputPanels = new ArrayList<>();
        // Create GridBagConstraints for flexible and fixed columns
        GridBagConstraints flexibleColumn = new GridBagConstraints();
        flexibleColumn.fill = GridBagConstraints.HORIZONTAL;
        flexibleColumn.weightx = 1.0;

        GridBagConstraints fixedColumn = new GridBagConstraints();
        fixedColumn.fill = GridBagConstraints.HORIZONTAL;
        fixedColumn.insets.set(5, 5, 5, 5);
        fixedColumn.weightx = 0.0; // Default value, just to be explicit


        GridBagConstraints panelConstraints = new GridBagConstraints();
        panelConstraints.fill = GridBagConstraints.HORIZONTAL;
        panelConstraints.weightx = 1.0;
        panelConstraints.insets.set(5, 5, 5, 5);
        for (int i = 0; i < files.length; i++) {
            int finalI = i;
            var crtInput = callback.getSelectedValues().get(files[finalI]);
            JPanel panel = new JPanel(new GridBagLayout());
            JBLabel fLabel = new JBLabel(files[i]);
            fLabel.setPreferredSize(new Dimension(70, fLabel.getPreferredSize().height));
            JBCheckBox fCheckBox = new JBCheckBox();
            fCheckBox.setSelected(crtInput.getTInclude());

            JPanel titlePanel = new JPanel(new GridBagLayout());
            flexibleColumn.insets.set(5, 5, 5, 5);
            titlePanel.add(fLabel, flexibleColumn);
            titlePanel.add(fCheckBox, fixedColumn);

            panelConstraints.gridy = 0;
            panel.add(titlePanel, panelConstraints);
            flexibleColumn.insets.set(5, 0, 5, 5);
            JLabel fNameLabel = new JLabel("File name:");
            fNameLabel.setPreferredSize(new Dimension(70, fNameLabel.getPreferredSize().height)); // Set fixed width

            JBTextField fNameField = new JBTextField(crtInput.getTName());
            JPanel namePanel = new JPanel(new GridBagLayout());

            fixedColumn.gridwidth = 1;
            namePanel.add(fNameLabel, fixedColumn);
            namePanel.add(fNameField, flexibleColumn);
            panelConstraints.gridy = 1;
            panel.add(namePanel, panelConstraints);


            fNameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    callback.onTextChange(files[finalI], fNameField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    callback.onTextChange(files[finalI], fNameField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    callback.onTextChange(files[finalI], fNameField.getText());
                }
            });
            //addActionListener(e -> callback.onTextChange(files[finalI], fNameField.getText()));


            JComboBox<String> comboBox = new ComboBox<>(options);
            JLabel label = new JLabel("Package:");
            label.setPreferredSize(new Dimension(70, label.getPreferredSize().height)); // Set fixed width
            JPanel packagePanel = new JPanel(new GridBagLayout());
            fixedColumn.gridwidth = 1;
            packagePanel.add(label, fixedColumn);
            packagePanel.add(comboBox, flexibleColumn);
            comboBox.setSelectedItem(crtInput.getTPackage());
            panelConstraints.gridy = 2;
            panel.add(packagePanel, panelConstraints);
            // Add an action listener to each combobox to update the callback
            comboBox.addActionListener(e -> callback.onComboBoxSelected(files[finalI], (String) comboBox.getSelectedItem()));
            Border border = BorderFactory.createLineBorder(JBColor.GRAY, 2);
            panel.setBorder(border);

            fCheckBox.addActionListener(e -> {
                namePanel.setVisible(fCheckBox.isSelected());
                packagePanel.setVisible(fCheckBox.isSelected());
                callback.onCheckBoxSelected(files[finalI], fCheckBox.isSelected());
            });

            inputPanels.add(panel);
        }

        return inputPanels;
    }


    private static class Callback {
        private final Map<String, TemplateInput> selectedValues;

        public Callback() {
            selectedValues = new HashMap<>();

        }

        public Callback(String[] files, String defaultPackage) {
            selectedValues = new HashMap<>();
            for (String file : files) {
                var sp = file.split("/");
                var fileName = sp[sp.length - 1];
                selectedValues.put(file, TemplateInput.builder().tName(fileName).tPackage(defaultPackage).tInclude(true).build());
            }

        }

        public void onComboBoxSelected(String file, String selectedPackage) {
            if (selectedValues.containsKey(file)) {
                var _selectedValue = selectedValues.get(file);
                _selectedValue.setTPackage(selectedPackage);
                selectedValues.put(file, _selectedValue);
            } else {
                selectedValues.put(file, TemplateInput.builder().tPackage(selectedPackage).build());
            }

        }

        public void onCheckBoxSelected(String file, Boolean selected) {
            if (selectedValues.containsKey(file)) {
                var _selectedValue = selectedValues.get(file);
                _selectedValue.setTInclude(selected);
                selectedValues.put(file, _selectedValue);
            } else {
                selectedValues.put(file, TemplateInput.builder().tInclude(selected).build());
            }

        }

        public void onTextChange(String file, String fileName) {
            if (selectedValues.containsKey(file)) {
                var _selectedValue = selectedValues.get(file);
                _selectedValue.setTName(fileName);
                selectedValues.put(file, _selectedValue);
            } else {
                selectedValues.put(file, TemplateInput.builder().tName(fileName).build());
            }

        }

        public Map<String, TemplateInput> getSelectedValues() {
            return selectedValues;
        }
    }

}






