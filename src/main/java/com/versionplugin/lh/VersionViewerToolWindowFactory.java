package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VersionViewerToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建版本表格
        String[] columnNames = {"版本时间", "内容", "修改者"};
        List<FileVersion> versions = new VersionManager().getVersions("filePath"); // 假设您有 filePath

        // 表格模型
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (FileVersion version : versions) {
            Object[] rowData = {
                    version.getTimestamp(),
                    version.getContent().substring(0, Math.min(version.getContent().length(), 50)), // 只显示前50个字符
                    version.getAuthor()
            };
            tableModel.addRow(rowData);
        }

        JBTable versionTable = new JBTable(tableModel);
        versionTable.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(versionTable);

        // 设置布局
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        // 使用 ToolWindow 的 setComponent 方法来设置内容
        toolWindow.getContentManager().addContent(toolWindow.getContentManager().getFactory().createContent(panel, "", false));
    }
}
