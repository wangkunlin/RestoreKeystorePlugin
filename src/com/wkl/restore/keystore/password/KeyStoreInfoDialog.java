package com.wkl.restore.keystore.password;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by wangkunlin
 * on 12/9/21.
 */
public class KeyStoreInfoDialog extends DialogWrapper {

    private final KeyStoreInfoPanel mCenterPanel = new KeyStoreInfoPanel();

    protected KeyStoreInfoDialog(@Nullable Project project) {
        super(project);
        setTitle("KeyStore Info");
        init();
        mCenterPanel.init(project);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mCenterPanel.getContentPanel();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        Action cancelAction = getCancelAction();
        cancelAction.putValue(Action.NAME, "Close");
        return new Action[]{cancelAction};
    }
}
