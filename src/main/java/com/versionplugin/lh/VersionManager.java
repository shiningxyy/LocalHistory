package com.versionplugin.lh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        addVersion(filePath, initialVersion);//路径和版本map
        System.out.println(initialVersion);
    }

    // 添加新版本
    public void addVersion(String filepath, FileVersion fileVersion) {
        versionMap.computeIfAbsent(filepath, k -> new ArrayList<>()).add(fileVersion);
    }

    // 获取指定文件的所有版本
    public List<FileVersion> getVersions(String filepath) {
        return versionMap.getOrDefault(filepath, new ArrayList<>());
    }

    // 获取最新版本
    public FileVersion getLatestVersion(String filepath) {
        List<FileVersion> versions = getVersions(filepath);
        if (!versions.isEmpty()) {
            return versions.get(versions.size() - 1); // 返回列表的最后一个版本
        }
        return null; // 如果没有版本，返回null
    }

    // 根据版本索引获取指定版本
    public FileVersion getVersionByIndex(String filepath, int index) {
        List<FileVersion> versions = getVersions(filepath);
        if (index >= 0 && index < versions.size()) {
            return versions.get(index);
        }
        return null; // 如果索引无效，返回null
    }


    public void rollbackVersion(String filepath,int number){
        List<FileVersion> versions = getVersions(filepath);
        FileVersion rollbackVer=versions.get(number);
        String rollbackCon= rollbackVer.getContent();

        System.out.println("回滚内容：\n"+rollbackCon);
        try {
            // 将新内容写入文件，覆盖原有内容
            Files.write(Paths.get(filepath), rollbackCon.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            //System.out.println("文件内容已回滚到版本 " + number);

            String currentContent = new String(Files.readAllBytes(Paths.get(filepath)));
            System.out.println("当前文件内容: \n" + currentContent);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("回滚操作失败: " + e.getMessage());
        }
    }

}
