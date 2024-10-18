package com.versionplugin.lh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public class UIComponents extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // 获取工具窗口管理器
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        // 获取工具窗口
        com.intellij.openapi.wm.ToolWindow toolWindow = toolWindowManager.getToolWindow("Version Viewer");
        if (toolWindow != null) {
            toolWindow.show(() -> {
                // 你可以在这里执行额外的初始化逻辑
            });
        }
    }
}
