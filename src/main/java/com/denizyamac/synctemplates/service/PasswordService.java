package com.denizyamac.synctemplates.service;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;

public class PasswordService {
    private static final String SERVICE_NAME = "com.denizyamac.sync-templates-plugin.PasswordService";

    public static PasswordService getInstance() {
        return ApplicationManager.getApplication().getService(PasswordService.class);
    }

    public void savePassword(String password) {
        PasswordSafe.getInstance().setPassword(new CredentialAttributes(SERVICE_NAME), password);
    }

    public String loadPassword() {
        return PasswordSafe.getInstance().getPassword(new CredentialAttributes(SERVICE_NAME));
    }
}