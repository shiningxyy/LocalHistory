package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;

public class VersionViewer implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        // 创建一个面板作为工具窗口内容
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Version Viewer");
        panel.add(label);

        // 获取 ContentFactory
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
