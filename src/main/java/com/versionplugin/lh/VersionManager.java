package com.versionplugin.lh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionManager {
    // 存储文件名与其版本列表的映射
    private final Map<String, List<FileVersion>> versionMap;

    public VersionManager() {
        versionMap = new HashMap<>();
    }
    // 初始化文件版本
    public void initializeFileVersion(String filename, String filePath, String initialContent) {
        FileVersion initialVersion = new FileVersion(filename, filePath, initialContent);
        addVersion(filename, initialVersion);
    }

    // 添加新版本
    public void addVersion(String fileName, FileVersion fileVersion) {
        List<FileVersion> versions = versionMap.get(fileName);

        if (versions == null) {
            // 如果文件名没有对应的版本列表，创建一个新的列表
            versions = new ArrayList<>();
            versionMap.put(fileName, versions);
        }

        // 添加新的版本到版本列表中
        versions.add(fileVersion);

        // 打印版本信息，调试时可以使用
        System.out.println("Added new version for file: " + fileName);
    }

    // 获取指定文件的所有版本
    public List<FileVersion> getVersions(String fileName) {
        return versionMap.getOrDefault(fileName, new ArrayList<>());
    }

    // 获取最新版本
    public FileVersion getLatestVersion(String fileName) {
        List<FileVersion> versions = getVersions(fileName);
        if (!versions.isEmpty()) {
            return versions.get(versions.size() - 1); // 返回列表的最后一个版本
        }
        return null; // 如果没有版本，返回null
    }

    // 根据版本索引获取指定版本
    public FileVersion getVersionByIndex(String fileName, int index) {
        List<FileVersion> versions = getVersions(fileName);
        if (index >= 0 && index < versions.size()) {
            return versions.get(index);
        }
        return null; // 如果索引无效，返回null
    }

    public boolean shouldSaveVersion(String fileName, String newContent, double threshold) {
        List<FileVersion> versions = getVersions(fileName);
        if (versions.isEmpty()) {
            return true; // 如果没有版本，保存
        }

        String lastVersionContent = versions.get(versions.size() - 1).getContent();
        double changeRatio = calculateChangeRatio(lastVersionContent, newContent);

        return changeRatio > threshold; // 返回是否超过阈值
    }

    // 计算内容变化比例
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
