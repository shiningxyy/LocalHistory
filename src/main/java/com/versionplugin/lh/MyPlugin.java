package com.versionplugin.lh;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.ProjectTopics;
import com.intellij.openapi.module.ModuleListener;

public class MyPlugin implements ProjectComponent {
    private final VersionManager versionManager;
    private final Project project;

    public MyPlugin(Project project) {
        this.project = project; // 获取当前项目
        this.versionManager = new VersionManager();
    }

    @Override
    public void initComponent() {
        // 注册项目管理监听器
        ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project openedProject) {
                if (openedProject.equals(project)) {
                    registerDocumentListener();
                }
            }

            @Override
            public void projectClosed(@NotNull Project closedProject) {
                if (closedProject.equals(project)) {
                    // 清理操作，如果需要
                }
            }
        });
    }

    @Override
    public void disposeComponent() {
        // 插件清理逻辑（如果需要）
    }

    private void registerDocumentListener() {
        FileEditorManager.getInstance(project).addFileEditorManagerListener(new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                FileEditor[] fileEditors = FileEditorManager.getInstance(project).getEditors(file);
                for (FileEditor fileEditor : fileEditors) {
                    if (fileEditor instanceof Editor) {
                        Editor editor = (Editor) fileEditor;
                        Document document = editor.getDocument();
                        if (document != null) {
                            document.addDocumentListener(new DocumentListener() {
                                @Override
                                public void beforeDocumentChange(DocumentEvent event) {
                                    String newContent = event.getDocument().getText();
                                    String filePath = getCurrentFilePath(); // 获取当前文件路径
                                    double changeThreshold = 0.1; // 设定变化阈值

                                    if (versionManager.shouldSaveVersion(filePath, newContent, changeThreshold)) {
                                        versionManager.addVersion(filePath, new FileVersion(filePath, newContent, "author")); // 替换 "author" 为实际的作者信息
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private String getCurrentFilePath() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor[] editors = fileEditorManager.getAllEditors();

        for (FileEditor editor : editors) {
            VirtualFile file = editor.getFile();
            if (file != null) {
                return file.getPath(); // 返回当前文件的路径
            }
        }
        return null; // 如果没有找到文件，返回 null
    }
}
