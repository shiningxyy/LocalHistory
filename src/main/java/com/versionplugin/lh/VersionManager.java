package com.versionplugin.lh;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionManager {

    // 用于保存每个文件的历史版本
    private final Map<String, List<FileVersion>> fileHistoryMap = new HashMap<>();

    public VersionManager() {
        // 注册文件监听器
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void contentsChanged(VirtualFileEvent event) {
                VirtualFile file = event.getFile();
                // 文件内容发生改变时保存版本
                saveVersion(file);
            }
        });
    }

    // 保存文件版本
    public void saveVersion(VirtualFile file) {
        try {
            String content = new String(file.contentsToByteArray());
            String timestamp = LocalDateTime.now().toString();
            FileVersion newVersion = new FileVersion(content, timestamp, file.getPath());

            fileHistoryMap.putIfAbsent(file.getPath(), new ArrayList<>());
            fileHistoryMap.get(file.getPath()).add(newVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取文件的历史版本
    public List<FileVersion> getFileHistory(String filePath) {
        return fileHistoryMap.getOrDefault(filePath, new ArrayList<>());
    }

    // 恢复指定的文件版本
    public FileVersion getSpecificVersion(String filePath, int versionIndex) {
        List<FileVersion> versions = getFileHistory(filePath);
        if (versionIndex >= 0 && versionIndex < versions.size()) {
            return versions.get(versionIndex);
        }
        return null;
    }
}
