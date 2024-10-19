package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;


public class VersionViewerToolWindowFactory implements ToolWindowFactory {

    private JBTable versionTable;
    private DefaultTableModel tableModel;
    private VersionManageActivity versionManageActivity; // 使用 VersionManageActivity

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 初始化 VersionManageActivity
        versionManageActivity = new VersionManageActivity();
        versionManageActivity.initializeFileVersions(project); // 初始化文件版本
        versionManageActivity.runActivity(project);

        // 创建版本表格
        String[] columnNames = {"File Name", "Version Time", "File Path", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        versionTable = new JBTable(tableModel);
        versionTable.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(versionTable);

        // 创建刷新按钮
        JButton refreshButton = new JButton("refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable(project);
            }
        });

        // 设置布局
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.NORTH); // 将刷新按钮放在上方

        // 使用 ToolWindow 的 setComponent 方法来设置内容
        toolWindow.getContentManager().addContent(
                toolWindow.getContentManager().getFactory().createContent(panel, "", false)
        );

        // 初始化表格内容
        refreshTable(project); // 初始化表格内容

        // 添加按钮渲染器和编辑器
        versionTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        versionTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), project));
    }

    // 刷新表格内容
    private void refreshTable(Project project) {
        // 清空现有表格数据
        tableModel.setRowCount(0);

        // 获取当前项目中的所有文件名
        List<String> files = VersionManageActivity.getAllFilePaths(project);
        System.out.println("文件列表: " + files);

        // 遍历每个文件，获取其版本并填充表格
        for (String filePath : files) {
            String fileName = new File(filePath).getName(); // 仅获取文件名

            List<FileVersion> versions = versionManageActivity.getVersionManager().getVersions(filePath);
            if (versions == null || versions.isEmpty()) {
                System.out.println("未找到版本信息，文件名: " + fileName);
                continue; // 跳过当前文件，继续下一个文件
            }

            System.out.println("文件路径: " + filePath + ", 版本数量: " + versions.size());

            for (FileVersion version : versions) {
                Object[] rowData = {
                        fileName,                        // 文件名
                        version.getTimestamp(),          // 版本时间
                        version.getFilePath(),           // 文件路径
                        "View Content"                   // 按钮显示的文字
                };
                tableModel.addRow(rowData); // 添加到表格中
            }
        }
    }

    // 自定义渲染器：用于显示按钮
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "View Content" : value.toString());
            return this;
        }
    }

    // 自定义编辑器：处理按钮点击事件
    class ButtonEditor extends DefaultCellEditor {

        private String label;
        private JButton button;
        private boolean isPushed;
        private Project project;

        public ButtonEditor(JCheckBox checkBox, Project project) {
            super(checkBox);
            this.project = project;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "View Content" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 在此处处理按钮的点击事件
                int row = versionTable.getSelectedRow();
                String filePath = (String) tableModel.getValueAt(row, 2); // 获取文件路径
                JOptionPane.showMessageDialog(button, "查看文件内容: " + filePath);
                // 在此处可以打开文件内容或执行其他操作
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

}
