package com.versionplugin.lh;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.roots.ProjectRootManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersionManageActivity {
    private final VersionManager versionManager;

    public VersionManageActivity() {
        this.versionManager = new VersionManager();
    }

    // 初始化文件版本
    public void initializeFileVersions(Project project) {
        List<VirtualFile> files = getAllFiles(project); // 获取当前项目中的所有文件
        System.out.println("文件列表: " + files);

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

        // 使用 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesRecursively(child, files); // 递归遍历子目录
            } else {
                files.add(child); // 添加文件到列表
            }
        }
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }
}
