package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VersionViewer extends DialogWrapper {
    private final Project project;
    private final VersionManager versionManager;
    private final String filePath;

    private JBTable versionTable;

    public VersionViewer(Project project, VersionManager versionManager, String filePath) {
        super(project);
        this.project = project;
        this.versionManager = versionManager;
        this.filePath = filePath;
        setTitle("版本查看器");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
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

        versionTable = new JBTable(tableModel);
        versionTable.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(versionTable);

        // 设置布局
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()}; // 提供OK和取消按钮
    }
}
