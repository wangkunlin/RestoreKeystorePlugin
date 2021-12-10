package com.wkl.restore.keystore.password;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by wangkunlin
 * on 12/9/21.
 */
public class MyAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        DataContext context = event.getDataContext();

        Project project = CommonDataKeys.PROJECT.getData(context);
        if (project == null) {
            return;
        }

        KeyStoreInfoDialog dialog = new KeyStoreInfoDialog(project);
        dialog.show();
    }
}
