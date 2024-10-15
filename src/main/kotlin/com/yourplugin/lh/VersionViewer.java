package com.yourplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class VersionViewer implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建 UI 界面，用于展示保存的版本列表
        List<String> savedVersions = getSavedVersions(project); // 获取保存的版本

        JBList<String> versionList = new JBList<>(savedVersions.toArray(new String[0]));
        versionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedVersion = versionList.getSelectedValue();
                Messages.showMessageDialog(project, "Selected version: " + selectedVersion, "Version Details", Messages.getInformationIcon());
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JScrollPane(versionList));
        toolWindow.getComponent().add(panel);
    }

    private List<String> getSavedVersions(Project project) {
        // 获取保存的版本（模拟数据，实际应从 Local History 获取）
        return List.of("Version 1", "Version 2", "Version 3");
    }
}