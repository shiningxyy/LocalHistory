package com.yourplugin.lh;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.history.LocalHistory;

import java.util.List;

@Service
public final class LocalHistoryManager {
    private final Project project;

    public LocalHistoryManager(Project project) {
        this.project = project;
    }

    public void startTracking() {
        // 监听文件更改
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            public void contentsChanged(VFileEvent event) {
                VirtualFile file = event.getFile();
                if (file != null) {
                    saveVersion(file);
                }
            }
        });
    }

    private void saveVersion(VirtualFile file) {
        // 使用 PSI 管理器获取文件
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            // 将当前文件内容保存到 Local History
            LocalHistory.getInstance().putUserLabel(project, "Saved version of: " + psiFile.getName());
        }
    }
}