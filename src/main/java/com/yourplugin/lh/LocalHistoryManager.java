package com.yourplugin.lh;

import com.intellij.history.LocalHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class LocalHistoryManager {
    private final Project project;
    private final VirtualFileListener fileListener;

    public LocalHistoryManager(@NotNull Project project) {
        this.project = project;
        this.fileListener = new VirtualFileListener() {
            public void contentsChanged(@NotNull VFileEvent event) {
                VirtualFile file = event.getFile();
                if (file != null && isSourceFile(file)) {
                    saveVersion(file);
                }
            }
        };
    }

    // 启动文件监听
    public void startTracking() {
        VirtualFileManager.getInstance().addVirtualFileListener(fileListener, project);
    }

    // 停止文件监听
    public void stopTracking() {
        VirtualFileManager.getInstance().removeVirtualFileListener(fileListener);
    }

    // 判断是否为源代码文件，可以根据需要调整
    private boolean isSourceFile(VirtualFile file) {
        String extension = file.getExtension();
        return extension != null && (extension.equals("java") || extension.equals("kt") || extension.equals("py")); // 根据需要添加更多扩展名
    }

    // 保存文件的当前版本到 Local History
    private void saveVersion(VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
            LocalHistory.getInstance().putSystemLabel(project, "Auto-saved version for: " + file.getName());
        }
    }
}
