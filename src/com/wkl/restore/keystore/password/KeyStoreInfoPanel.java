package com.wkl.restore.keystore.password;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.exportSignedPackage.GenerateSignedApkSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by wangkunlin
 * on 12/9/21.
 */
public class KeyStoreInfoPanel {

    private static final String KEY_STORE_PASSWORD_KEY = "KEY_STORE_PASSWORD";
    private static final String KEY_PASSWORD_KEY = "KEY_PASSWORD";

    private JPanel mContentPanel;
    private JTextField mKeyStorePathField;
    private JTextField mKeyStorePasswordField;
    private JTextField mKeyAliasField;
    private JTextField mKeyPasswordField;

    public JPanel getContentPanel() {
        return mContentPanel;
    }

    public void init(Project project) {
        // https://cs.android.com/android-studio/platform/tools/adt/idea/+/mirror-goog-studio-main:project-system-gradle/src/org/jetbrains/android/exportSignedPackage/KeystoreStep.java
        executeInBackground(() -> {
            GenerateSignedApkSettings settings = GenerateSignedApkSettings.getInstance(project);
            if (settings == null) {
                return;
            }
            String keyStorePath = settings.KEY_STORE_PATH;
            mKeyStorePathField.setText(keyStorePath);

            String keyAlias = settings.KEY_ALIAS;
            mKeyAliasField.setText(keyAlias);

            if (!settings.REMEMBER_PASSWORDS) {
                return;
            }

            PasswordSafe passwordSafe = PasswordSafe.getInstance();

            String keyStorePasswordKey = makePasswordKey(KEY_STORE_PASSWORD_KEY, keyStorePath, null);
            String keyPasswordKey = makePasswordKey(KEY_PASSWORD_KEY, keyStorePath, keyAlias);

            retrievePassword(passwordSafe, Arrays.asList(
                    credentialAttributesForKey(keyStorePasswordKey),
                    createKeystoreDeprecatedAttributesPre_2021_1_1_3(keyStorePasswordKey),
                    createDeprecatedAttributesPre_3_2(keyStorePasswordKey)
            )).map(Credentials::getPassword).ifPresent(password ->
                    mKeyStorePasswordField.setText(password.toString()));

            retrievePassword(passwordSafe, Arrays.asList(
                    credentialAttributesForKey(keyPasswordKey),
                    createKeyDeprecatedAttributesPre_2021_1_1_3(keyPasswordKey),
                    createDeprecatedAttributesPre_3_2(keyPasswordKey)
            )).map(Credentials::getPassword).ifPresent(password ->
                    mKeyPasswordField.setText(password.toString()));
        });
    }

    private static String makePasswordKey(@NotNull String prefix, @NotNull String keyStorePath, @Nullable String keyAlias) {
        return prefix + "__" + keyStorePath + (keyAlias != null ? "__" + keyAlias : "");
    }

    /**
     * This is the new recommended way to create CredentialAttributes.
     * Usage of accessor class for creating CredentialAttributes is deprecated.
     * We need to include key to the service name to be able to save passwords for several keystores/key aliases.
     * PasswordSafe does not attempt to find the correct credentials by username internally,
     * thus only one username/password pair can be saved per service name.
     * That's why we need to include password determining key into service name instead of passing it as user name.
     */
    private static @NotNull
    CredentialAttributes credentialAttributesForKey(@NotNull String key) {
        String serviceName = CredentialAttributesKt.generateServiceName("APK Signing Keystore Step", key);
        return new CredentialAttributes(serviceName);
    }

    /**
     * Deprecated way to create attributes that was used from Studio ~3.2 to Bumblebee Canary 3 (2021.1.1.3).
     * Left for migrating passwords from Studio before that version.
     */
    private static @NotNull
    CredentialAttributes createKeystoreDeprecatedAttributesPre_2021_1_1_3(@NotNull String key) {
        return new CredentialAttributes("org.jetbrains.android.exportSignedPackage.KeystoreStep$KeyStorePasswordRequestor", key);
    }

    /**
     * Deprecated way to create attributes that was used before Studio 3.2.
     * Left for migrating passwords from Studio before that version.
     */
    private static @NotNull
    CredentialAttributes createDeprecatedAttributesPre_3_2(@NotNull String key) {
        return new CredentialAttributes("org.jetbrains.android.exportSignedPackage.KeystoreStep", key);
    }

    /**
     * Deprecated way to create attributes that was used from Studio ~3.2 to Bumblebee Canary 3 (2021.1.1.3).
     * Left for migrating passwords from Studio before that version.
     */
    private static @NotNull
    CredentialAttributes createKeyDeprecatedAttributesPre_2021_1_1_3(@NotNull String key) {
        return new CredentialAttributes("org.jetbrains.android.exportSignedPackage.KeystoreStep$KeyPasswordRequestor", key);
    }

    private static @NotNull
    Optional<Credentials> retrievePassword(@NotNull PasswordSafe passwordSafe,
                                           @NotNull List<CredentialAttributes> credentialAttributesToTry) {
        return credentialAttributesToTry.stream()
                .map(attributes -> Optional.ofNullable(passwordSafe.get(attributes)))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    private static void executeInBackground(Runnable runnable) {
        runnable = new CatchExceptionRunnable(runnable);
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            runnable.run();
        } else {
            ApplicationManager.getApplication().executeOnPooledThread(runnable);
        }
    }

    private static class CatchExceptionRunnable implements Runnable {

        private final Runnable mRunnable;

        private CatchExceptionRunnable(@NotNull Runnable runnable) {
            mRunnable = runnable;
        }

        @Override
        public void run() {
            try {
                mRunnable.run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
