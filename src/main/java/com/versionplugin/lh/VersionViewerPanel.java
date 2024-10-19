package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VersionViewerPanel extends SimpleToolWindowPanel {
    private final VersionManager versionManager;
    private final String filePath;

    public VersionViewerPanel(Project project, VersionManager versionManager, String filePath) {
        super(true, true);
        this.versionManager = versionManager;
        this.filePath = filePath;
        setToolbar(createToolbar()); // 如果需要工具栏
        setContent(createCenterPanel()); // 设置主内容面板
    }

    private JComponent createCenterPanel() {
        // 创建版本表格
        String[] columnNames = {"版本时间", "内容", "修改者"};
        List<FileVersion> versions = versionManager.getVersions(filePath);

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

        return panel;
    }

    private JComponent createToolbar() {
        // 创建工具栏，可以添加按钮
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshTable());
        toolbarPanel.add(refreshButton);
        return toolbarPanel;
    }

    private void refreshTable() {
        // 刷新版本表格内容
        // 可根据实际需要重新获取数据并更新表格
    }
}
