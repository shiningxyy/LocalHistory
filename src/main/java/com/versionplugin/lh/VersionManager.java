package com.versionplugin.lh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionManager {
    // 存储文件路径与其版本列表的映射
    private final Map<String, List<FileVersion>> versionMap;

    public VersionManager() {
        versionMap = new HashMap<>();
    }

    // 添加新版本
    public void addVersion(String filePath, FileVersion fileVersion) {
        versionMap.computeIfAbsent(filePath, k -> new ArrayList<>()).add(fileVersion);
    }//将新的 fileVersion 添加到该列表中

    // 获取指定文件的所有版本
    public List<FileVersion> getVersions(String filePath) {
        return versionMap.getOrDefault(filePath, new ArrayList<>());
    }

    // 获取最新版本
    public FileVersion getLatestVersion(String filePath) {
        List<FileVersion> versions = getVersions(filePath);
        if (!versions.isEmpty()) {
            return versions.get(versions.size() - 1); // 返回列表的最后一个版本，假设是最新的
        }
        return null; // 如果没有版本，返回null
    }

    // 根据版本索引获取指定版本
    public FileVersion getVersionByIndex(String filePath, int index) {
        List<FileVersion> versions = getVersions(filePath);
        if (index >= 0 && index < versions.size()) {
            return versions.get(index);
        }
        return null; // 如果索引无效，返回null
    }

    public boolean shouldSaveVersion(String filePath, String newContent, double threshold) {
        List<FileVersion> versions = getVersions(filePath);
        if (versions.isEmpty()) {
            return true; // 如果没有版本，保存
        }

        String lastVersionContent = versions.get(versions.size() - 1).getContent();
        double changeRatio = calculateChangeRatio(lastVersionContent, newContent);

        return changeRatio > threshold; // 返回是否超过阈值
    }

    // 计算内容变化比例的示例方法
    private double calculateChangeRatio(String oldContent, String newContent) {
        int oldLength = oldContent.length();
        int newLength = newContent.length();
        int changes = 0;

        // 计算变化的字符数
        for (int i = 0; i < Math.min(oldLength, newLength); i++) {
            if (oldContent.charAt(i) != newContent.charAt(i)) {
                changes++;
            }
        }

        changes += Math.abs(oldLength - newLength); // 计算长度差异
        return (double) changes / Math.max(oldLength, newLength); // 返回变化比例
    }

}
