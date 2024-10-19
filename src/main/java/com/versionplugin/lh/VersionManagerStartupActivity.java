package com.versionplugin.lh;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class VersionManagerStartupActivity implements StartupActivity {
    private final VersionManager versionManager;

    public VersionManagerStartupActivity() {
        this.versionManager = new VersionManager();
        System.out.println("运行Activity");
    }

    @Override
    public void runActivity(@NotNull Project project) {
        registerDocumentListener(project);
        registerSaveListener(project); // Register the save listener
    }

    private void registerDocumentListener(Project project) {
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
                                public void beforeDocumentChange(@NotNull DocumentEvent event) {
                                    String newContent = event.getDocument().getText();
                                    String filePath = getCurrentFilePath(project); // 获取当前文件路径
                                    String filename=getFileName(project);
                                    double changeThreshold = 0.1; // 设定变化阈值

                                    if (versionManager.shouldSaveVersion(filePath, newContent, changeThreshold)) {
                                        versionManager.addVersion(filename, new FileVersion(filename,filePath, newContent)); // 添加版本
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void registerSaveListener(Project project) {
        // Attach a listener to the FileDocumentManager to detect save events
        ApplicationManager.getApplication().getMessageBus().connect(project)
                .subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerListener() {
                    @Override
                    public void beforeDocumentSaving(@NotNull Document document) {
                        String newContent = document.getText();
                        String filePath = getCurrentFilePath(project); // 获取当前文件路径
                        System.out.println("监听器");
                        System.out.println(filePath);

                        versionManager.addVersion(filePath, new FileVersion(filePath, newContent, "author"));
                    }
                });
    }

    private String getCurrentFilePath(Project project) {
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

    // 获取当前文件的文件名
    private String getFileName(Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor[] editors = fileEditorManager.getAllEditors();

        for (FileEditor editor : editors) {
            VirtualFile file = editor.getFile();
            if (file != null) {
                return file.getName(); // 返回当前文件的文件名
            }
        }
        return null; // 如果没有找到文件，返回 null
    }
}