package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionViewerToolWindowFactory implements ToolWindowFactory {

    private JBTable versionTable;
    private DefaultTableModel tableModel;
    private VersionManager versionManager;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 初始化 VersionManager
        versionManager = new VersionManager();
        VersionManagerStartupActivity VMSA=new VersionManagerStartupActivity();
        VMSA.runActivity(project);
        // 创建版本表格
        String[] columnNames = {"文件名", "版本时间", "文件路径"};
        tableModel = new DefaultTableModel(columnNames, 0);
        versionTable = new JBTable(tableModel);
        versionTable.setFillsViewportHeight(true);
        JBScrollPane scrollPane = new JBScrollPane(versionTable);

        // 创建刷新按钮
        JButton refreshButton = new JButton("刷新");
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
        // 获取当前项目中的所有文件
        List<VirtualFile> files = getAllFiles(project); //方法返回项目中的所有 VirtualFile
        System.out.println("文件列表: " + files);

        // 清空现有表格数据
        tableModel.setRowCount(0);

        // 遍历每个文件，获取其版本并填充表格
        for (VirtualFile file : files) {
            String fileName = file.getName(); // 获取文件名
            String filePath = file.getPath(); // 获取文件路径
            System.out.println("文件名: " + fileName);

            // 获取文件内容
            String initialContent = "";

            try {
                initialContent = new String(file.contentsToByteArray()); // 使用 VirtualFile 获取文件内容
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            versionManager.initializeFileVersion(fileName, filePath, initialContent); // 创建初始版本


            // 获取版本信息并填充表格
            List<FileVersion> versions = versionManager.getVersions(fileName);
            if (versions == null || versions.isEmpty()) {
                System.out.println("未找到版本信息，文件名: " + fileName);
                continue; // 跳过当前文件，继续下一个文件
            }

            System.out.println("文件名: " + fileName + ", 版本数量: " + versions.size());

            // 填充表格
            for (FileVersion version : versions) {
                Object[] rowData = {
                        fileName,
                        version.getTimestamp(),
                        version.getFilePath()
                };
                tableModel.addRow(rowData); // 添加到表格中
            }
        }
    }


    // 刷新表格内容
    private void refreshTable(Project project) {
        // 获取当前项目中的所有文件名
        List<String> files = getAllFileNames(project);
        System.out.println("文件列表: " + files);

        // 清空现有表格数据
        tableModel.setRowCount(0);

        // 遍历每个文件，获取其版本并填充表格
        for (String filePath : files) {
            String fileName = new File(filePath).getName(); // 仅获取文件名
            System.out.println("文件名: " + fileName);

            List<FileVersion> versions = versionManager.getVersions(fileName);
            if (versions == null) {
                System.out.println("未找到版本信息，文件名: " + fileName);
                continue; // 跳过当前文件，继续下一个文件
            }
            System.out.println("文件名: " + fileName + ", 版本数量: " + versions.size());

            for (FileVersion version : versions) {
                Object[] rowData = {
                        fileName,                        // 文件名
                        version.getTimestamp(),          // 版本时间
                        version.getFilePath()            // 文件路径
                };
                System.out.println("文件名: " + fileName + ", 版本数量: " + versions.size());

                tableModel.addRow(rowData); // 添加到表格中
            }
        }

    }

    // 获取当前项目的所有文件名
    public static List<String> getAllFileNames(Project project) {
        List<String> fileNames = new ArrayList<>();

        // 获取项目的根目录
        VirtualFile[] projectRoots = ProjectRootManager.getInstance(project).getContentRoots();

        for (VirtualFile root : projectRoots) {
            // 遍历项目根目录下的所有文件和文件夹
            visitFilesNameRecursively(root, fileNames);
        }

        return fileNames;
    }

    // 递归遍历文件夹并添加文件名到列表
    private static void visitFilesNameRecursively(@NotNull VirtualFile root, List<String> fileNames) {
        root.refresh(false, true);  // 确保文件系统是最新的

        VirtualFileVisitor<Void> visitor = new VirtualFileVisitor<>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                // 如果是文件而不是目录，获取文件名并添加到列表
                if (!file.isDirectory()) {
                    fileNames.add(file.getPath()); // 使用文件的完整路径
                }
                return true; // 继续遍历
            }
        };

        // 使用传统的 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesNameRecursively(child, fileNames); // 递归遍历子目录
            } else {
                visitor.visitFile(child); // 访问文件
            }
        }
    }
    // 获取当前项目的所有 VirtualFile
    public static List<VirtualFile> getAllFiles(Project project) {
        List<VirtualFile> files = new ArrayList<>();

        // 获取项目的根目录
        VirtualFile[] projectRoots = ProjectRootManager.getInstance(project).getContentRoots();

        for (VirtualFile root : projectRoots) {
            // 遍历项目根目录下的所有文件和文件夹
            visitFilesRecursively(root, files);
        }

        return files;
    }

    // 递归遍历文件夹并添加文件到列表
    private static void visitFilesRecursively(@NotNull VirtualFile root, List<VirtualFile> files) {
        root.refresh(false, true);  // 确保文件系统是最新的

        // 使用传统的 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesRecursively(child, files); // 递归遍历子目录
            } else {
                files.add(child); // 添加文件到列表
            }
        }
    }

}