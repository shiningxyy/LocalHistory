package com.yourplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class VersionViewer implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 获取 Local History 的版本列表
        List<String> versions = getSavedVersions(project);

        // 创建列表组件
        JBList<String> versionList = new JBList<>(versions.toArray(new String[0]));
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 添加选择监听器
        versionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedVersion = versionList.getSelectedValue();
                if (selectedVersion != null) {
                    Messages.showMessageDialog(project, "Selected version: " + selectedVersion, "Version Details", Messages.getInformationIcon());
                    // 这里可以添加查看版本差异或恢复版本的逻辑
                }
            }
        });

        // 创建面板并添加组件
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(versionList));

        toolWindow.getContentManager().addContent(
                toolWindow.getContentManager().getFactory().createContent(panel, "", false)
        );
    }

    // 获取保存的版本列表（示例数据，实际应从 Local History 获取）
    private List<String> getSavedVersions(Project project) {
        List<String> versions = new ArrayList<>();
        // 这里可以通过 Local History API 获取实际的版本列表
        // 目前使用模拟数据
        versions.add("Version 1 - 2024-04-25 10:00");
        versions.add("Version 2 - 2024-04-25 11:00");
        versions.add("Version 3 - 2024-04-25 12:00");
        return versions;
    }
}
