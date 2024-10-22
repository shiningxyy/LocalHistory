package com.versionplugin.lh;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import javax.swing.table.*;


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
        String[] columnNames = {"File Name", "Version Time", "File Path", "Version Number", "View Content", "Rollback"};
        tableModel = new DefaultTableModel(columnNames, 0);
        versionTable = new JBTable(tableModel);
        versionTable.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(versionTable);

        // 创建刷新按钮
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable(project);
            }
        });

        // 创建提交按钮
        JButton commitButton = new JButton("Commit");
        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentFilePath =getCurrentFilePath(project);
                if (currentFilePath != null) {
                    // 弹出对话框让用户输入提交信息
                    String commitMessage = JOptionPane.showInputDialog(null, "Enter commit message:", "Commit Changes", JOptionPane.PLAIN_MESSAGE);
                    if (commitMessage != null && !commitMessage.trim().isEmpty()) {
                        try {
                            versionManageActivity.getGitCommandRunner().squashAndMergeFineGrainedCommits(project.getBasePath(),"main", "fine-grained-branch", commitMessage);
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Commit failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No file is currently open.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                System.out.println("Committing changes...");
                // TODO: 添加具体的提交逻辑
            }
        });

        // 设置布局
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(commitButton); // 添加提交按钮

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.NORTH); // 将按钮面板放在上方

        // 使用 ToolWindow 的 setComponent 方法来设置内容
        toolWindow.getContentManager().addContent(
                toolWindow.getContentManager().getFactory().createContent(panel, "", false)
        );

        // 初始化表格内容
        refreshTable(project); // 初始化表格内容

        // 添加按钮渲染器和编辑器
        versionTable.getColumn("View Content").setCellRenderer(new ButtonRenderer());
        versionTable.getColumn("View Content").setCellEditor(new ButtonEditor(new JCheckBox(), project));

        // 添加回滚按钮的渲染器和编辑器
        versionTable.getColumn("Rollback").setCellRenderer(new ButtonRenderer());
        versionTable.getColumn("Rollback").setCellEditor(new ButtonEditor(new JCheckBox(), project));
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
            if (fileName.startsWith(".") || filePath.contains(".git")) {
                continue; // 跳过当前文件，继续下一个文件
            }
            List<FileVersion> versions = versionManageActivity.getVersionManager().getVersions(filePath);
            if (versions == null || versions.isEmpty()) {
                System.out.println("未找到版本信息，文件名: " + fileName);
                continue; // 跳过当前文件，继续下一个文件
            }

            System.out.println("文件路径: " + filePath + ", 版本数量: " + versions.size());
            int index = 1;
            for (FileVersion version : versions) {
                Object[] rowData = {
                        fileName,                        // 文件名
                        version.getTimestamp(),          // 版本时间
                        version.getFilePath(),           // 文件路径
                        index,                           // 版本号
                        "View",                  // 查看内容按钮
                        "Rollback"                      // 回滚按钮
                };
                tableModel.addRow(rowData); // 添加到表格中
                index++; // 递增版本号
            }
        }
    }
    public String getCurrentFilePath(Project project) {
        // 获取当前打开的文件
        VirtualFile[] openFiles = FileEditorManager.getInstance(project).getSelectedFiles();

        if (openFiles.length > 0) {
            VirtualFile currentFile = openFiles[0]; // 获取第一个打开的文件
            return currentFile.getPath(); // 返回文件的路径
        }

        return null; // 如果没有打开的文件，返回null
    }
    // 自定义渲染器：用于显示按钮
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Button" : value.toString());
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
            label = (value == null) ? "Button" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = versionTable.getSelectedRow();
                String fileName = (String) tableModel.getValueAt(row, 0);
                String filePath = (String) tableModel.getValueAt(row, 2); // 获取文件路径
                int versionNumber = (int) tableModel.getValueAt(row, 3); // 获取文件版本号

                List<FileVersion> fileContent = versionManageActivity.getVersionManager().getVersions(filePath);
                int currentNumber = versionManageActivity.getVersionManager().getCurrentVersion(filePath);
                // 区分操作列，处理不同的按钮点击事件
                if (label.equals("View")) {
                    // 查看内容按钮操作
                    versionManageActivity.getVersionManager().compareVersion(filePath, versionNumber - 1, currentNumber - 1);
                } else if (label.equals("Rollback")) {
                    // 回滚按钮操作
                    if (Objects.equals(getCurrentFilePath(project), filePath)) {
                        versionManageActivity.getVersionManager().rollbackVersion(filePath, versionNumber - 1);

                        refreshEditor(project, filePath);
                        JOptionPane.showMessageDialog(button,
                                "The file has been rolled back to version: " + versionNumber, // 文件已回滚到版本
                                "Rollback operation successful", // 回滚操作成功
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        // 如果文件路径不同，弹出警告框
                        JOptionPane.showMessageDialog(button,
                                "You cannot roll back this file to the current file!", // 你不能将这个文件回滚到当前文件上！
                                "Rollback operation failed", // 回滚操作失败
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
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

        // 刷新打开的文件编辑器内容
        public void refreshEditor(Project project, String filePath) {
            // 获取 VirtualFile 对象
            VirtualFile virtualFile = FileEditorManager.getInstance(project).getSelectedFiles()[0]; // 当前选中文件
            if (virtualFile == null) {
                return; // 如果没有打开的文件，直接返回
            }

            // 刷新文件的内容
            try {
                // 重新加载文件内容
                virtualFile.refresh(false, false);

                // 通过 Document API 获取该文件的内容
                @Nullable Document document = FileDocumentManager.getInstance().getDocument(virtualFile);

                if (document != null) {
                    String newContent = new String(Files.readAllBytes(Paths.get(filePath))); // 读取最新内容

                    // 使用 WriteCommandAction 修改文件内容
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        document.setText(newContent); // 更新内容
                    });

                    // 重新加载编辑器中的文件
                    FileEditorManager.getInstance(project).openFile(virtualFile, true);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        public String getCurrentFilePath(Project project) {
            // 获取当前打开的文件
            VirtualFile[] openFiles = FileEditorManager.getInstance(project).getSelectedFiles();

            if (openFiles.length > 0) {
                VirtualFile currentFile = openFiles[0]; // 获取第一个打开的文件
                return currentFile.getPath(); // 返回文件的路径
            }

            return null; // 如果没有打开的文件，返回null
        }
    }

}

