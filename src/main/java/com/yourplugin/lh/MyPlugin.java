package com.yourplugin.lh;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyPlugin implements ProjectComponent {
    private final Project project;
    private LocalHistoryManager localHistoryManager;

    public MyPlugin(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        // 插件启用时启动版本跟踪
        localHistoryManager = new LocalHistoryManager(project);
        localHistoryManager.startTracking();
    }

    @Override
    public void projectClosed() {
        // 插件禁用时停止版本跟踪
        if (localHistoryManager != null) {
            localHistoryManager.stopTracking();
        }
    }

    @Override
    public void initComponent() {
        // 初始化组件
    }

    @Override
    public void disposeComponent() {
        // 释放资源
    }
}
