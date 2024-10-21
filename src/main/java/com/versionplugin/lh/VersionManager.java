package com.versionplugin.lh;

import com.google.gson.*;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class VersionManager {
    // 存储文件名与其版本列表的映射
    private final Map<String, List<FileVersion>> versionMap;
    // 存储文件路径与当前版本号的映射
    private final Map<String, Integer> currentVersionMap;
    private final Gson gson = new Gson(); // 用于处理JSON

    public VersionManager() {
        versionMap = new HashMap<>();
        currentVersionMap = new HashMap<>(); // 初始化当前版本映射
    }


    // 初始化文件版本
    public ArrayList<FileVersion> initializeFileVersion(String filename, String filePath, String initialContent,
                                                        ArrayList<Integer> versionNumbers, ArrayList<String> contents) {
        String versionFilePath = filePath + "_version.json"; // 指定保存版本号的文件
        int versionNumber;
        ArrayList<FileVersion>fileVersionArrayList=new ArrayList<>();
        // 读取版本信息文件
        File versionFile = new File(versionFilePath);
        if (versionFile.exists()) {
            try (Reader reader = Files.newBufferedReader(Paths.get(versionFilePath))) {
                JsonElement jsonElement = gson.fromJson(reader, JsonElement.class); // 先解析为 JsonElement

                if (jsonElement.isJsonObject()) {
                    // 处理 JsonObject 的情况
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    versionNumber = jsonObject.get("version").getAsInt(); // 获取当前版本号
                    String Content=jsonObject.get("content").getAsString();
                    versionNumbers.add(versionNumber);
                    contents.add(Content);
                    // 生成版本信息
                    FileVersion initialVersion = new FileVersion(filename, filePath, initialContent, versionNumber);
                    // 保存当前版本信息
                    //addVersion(filePath, initialVersion); // 路径和版本 map
                    fileVersionArrayList.add(initialVersion);
                    currentVersionMap.put(filePath, versionNumber); // 将当前版本号放入 map
                }
                else if (jsonElement.isJsonArray()) {
                    // 处理 JsonArray 的情况
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    boolean isFirstIteration = true; // 标记第一次循环

                    for (JsonElement element : jsonArray) {
                        JsonObject versionObject = element.getAsJsonObject();
                        // 根据你的需求解析每个版本的内容，比如版本号和内容
                        int arrayVersionNumber = versionObject.get("version").getAsInt();
                        String arrayContent = versionObject.get("content").getAsString();

                        versionNumbers.add(arrayVersionNumber);
                        contents.add(arrayContent);

                        FileVersion initialVersion = new FileVersion(filename, filePath, arrayContent, arrayVersionNumber);

                        if (!isFirstIteration) {
                            // 保存当前版本信息，跳过第一次循环
                            addVersion(filePath, initialVersion); // 路径和版本 map
                        }

                        fileVersionArrayList.add(initialVersion);
                        currentVersionMap.put(filePath, arrayVersionNumber); // 将当前版本号放入 map

                        // 标记第一次循环已完成
                        isFirstIteration = false;
                    }

                }
                else {
                    throw new RuntimeException("Unexpected JSON type in version file: " + versionFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("无法读取版本文件: " + versionFilePath, e);
            }
        }
        else {
            // 如果文件不存在，创建新版本文件
            try {
                Files.createFile(Paths.get(versionFilePath));
            } catch (IOException e) {
                throw new RuntimeException("无法创建版本文件: " + versionFilePath, e);
            }
            versionNumber = 1; // 默认初始版本号为1
            // 生成版本信息
            FileVersion initialVersion = new FileVersion(filename, filePath, initialContent, versionNumber);
            // 保存当前版本信息
            addVersion(filePath, initialVersion); // 路径和版本 map
            fileVersionArrayList.add(initialVersion);
            // 更新版本文件
            //saveVersionToFile(versionFilePath, versionNumber, initialContent);
            currentVersionMap.put(filePath, versionNumber); // 将当前版本号放入 map
        }

       // System.out.println(initialVersion);
        return fileVersionArrayList;
    }


    private void saveVersionToFile(String versionFilePath, int versionNumber, String content) {
        JsonArray versionsArray = new JsonArray();

        // 尝试读取现有文件内容
        try {
            String jsonString = Files.readString(Paths.get(versionFilePath));
            JsonElement jsonElement = JsonParser.parseString(jsonString);

            // 检查读取到的元素是否是JsonArray
            if (jsonElement.isJsonArray()) {
                versionsArray = jsonElement.getAsJsonArray();
            }
        } catch (IOException e) {
            // 如果文件不存在或读取出错，可以选择继续
        } catch (JsonSyntaxException e) {
            // 如果解析出错，可能是文件格式不正确，清空数组以避免使用旧数据
            versionsArray = new JsonArray();
        }

        // 创建新的版本对象并添加到数组
        JsonObject newVersion = new JsonObject();
        newVersion.addProperty("version", versionNumber);
        newVersion.addProperty("content", content);
        versionsArray.add(newVersion);

        // 写回文件
        try (Writer writer = Files.newBufferedWriter(Paths.get(versionFilePath))) {
            gson.toJson(versionsArray, writer);
        } catch (IOException e) {
            throw new RuntimeException("无法保存版本文件: " + versionFilePath, e);
        }
    }




    // 添加新版本
    public void addVersion(String filepath, FileVersion fileVersion) {
        versionMap.computeIfAbsent(filepath, k -> new ArrayList<>()).add(fileVersion);
        // 更新当前版本号
        currentVersionMap.put(filepath, versionMap.get(filepath).size() ); // 设置为最新版本索引
        int versionNum=fileVersion.getVersionNum();
        String content=fileVersion.getContent();
        saveVersionToFile(filepath+"_version.json",versionNum,content);
    }

    // 获取指定文件的所有版本
    public List<FileVersion> getVersions(String filepath) {
        return versionMap.getOrDefault(filepath, new ArrayList<>());
    }
    public FileVersion getLatestVersion(String filePath) {
        List<FileVersion> versions = getVersions(filePath);
        if (!versions.isEmpty()) {
            return versions.get(versions.size() - 1); // 获取最后一个版本，作为最新版本
        } else {
            return null; // 如果没有版本，则返回null
        }
    }

    public int getCurrentVersion(String filepath) {
        return currentVersionMap.getOrDefault(filepath, -1); // 返回当前版本号，未找到返回-1
    }

    public Set<String> getFilenames() {
        return versionMap.keySet();
    }

    public String rollbackVersion(String filepath, int number) {
        List<FileVersion> versions = getVersions(filepath);
        FileVersion rollbackVer = versions.get(number);
        String rollbackCon = rollbackVer.getContent();

        System.out.println("回滚内容：\n" + rollbackCon);
        try {
            // 将新内容写入文件，覆盖原有内容
            Files.write(Paths.get(filepath), rollbackCon.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("文件内容已回滚到版本 " + number);
            currentVersionMap.put(filepath, number+1); // 更新当前版本号为回滚后的版本
            // 输出当前文件路径的版本号
            System.out.println("当前文件路径: " + filepath + " 的版本号: " + currentVersionMap.get(filepath));
            System.out.println("总版本数量: " + getVersions(filepath).size());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("回滚操作失败: " + e.getMessage());
        }
        return rollbackCon;
    }

    public void compareVersion(String filepath, int thisNum, int currentNum) {
        // 获取版本内容
        List<FileVersion> versions = getVersions(filepath);
        FileVersion thisVer = versions.get(thisNum);
        FileVersion currentVer = versions.get(currentNum);
        String thisContent = thisVer.getContent();
        String currentContent = currentVer.getContent();

        // 调用方法创建并显示对比窗口
        showCompareDialog(thisContent, currentContent);
    }

    // 创建对比窗口，带有高亮差异的功能
    private void showCompareDialog(String thisContent, String currentContent) {
        // 创建对话框
        JDialog dialog = new JDialog();
        dialog.setTitle("版本比较（左侧为比较版本，右侧为当前版本）");
        dialog.setSize(800, 600); // 设置窗口大小
        dialog.setLocationRelativeTo(null); // 居中显示

        // 创建左边的 JTextPane 显示 thisContent
        JTextPane leftTextPane = createTextPaneWithDiff(thisContent, currentContent, true);
        JScrollPane leftScrollPane = new JScrollPane(leftTextPane);

        // 创建右边的 JTextPane 显示 currentContent
        JTextPane rightTextPane = createTextPaneWithDiff(thisContent, currentContent, false);
        JScrollPane rightScrollPane = new JScrollPane(rightTextPane);

        // 创建水平分割面板，将两个滚动面板放入
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerLocation(400); // 设置分割条位置
        splitPane.setResizeWeight(0.5); // 使两边均分窗口空间

        // 将分割面板添加到对话框中
        dialog.add(splitPane);

        // 显示对话框
        dialog.setVisible(true);
    }

    // 创建 JTextPane 并高亮显示差异
    private JTextPane createTextPaneWithDiff(String thisContent, String currentContent, boolean isLeft) {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false); // 设置不可编辑
        StyledDocument doc = textPane.getStyledDocument();

        // 样式设置
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        // 样式：相同行（白色字体）
        Style sameStyle = doc.addStyle("Same", defaultStyle);
        StyleConstants.setForeground(sameStyle, Color.WHITE);

        // 样式：左侧不同内容（红色字体）
        Style diffLeftStyle = doc.addStyle("DiffLeft", defaultStyle);
        StyleConstants.setForeground(diffLeftStyle, Color.RED);

        // 样式：右侧不同内容（绿色字体）
        Style diffRightStyle = doc.addStyle("DiffRight", defaultStyle);
        StyleConstants.setForeground(diffRightStyle, Color.GREEN);

        // 按行拆分并逐行比较
        String[] thisLines = thisContent.split("\n");
        String[] currentLines = currentContent.split("\n");
        int maxLines = Math.max(thisLines.length, currentLines.length);

        for (int i = 0; i < maxLines; i++) {
            String thisLine = i < thisLines.length ? thisLines[i] : "";
            String currentLine = i < currentLines.length ? currentLines[i] : "";

            try {
                // 左侧文本: 显示 thisContent
                if (isLeft) {
                    if (thisLine.equals(currentLine)) {
                        doc.insertString(doc.getLength(), thisLine + "\n", sameStyle); // 相同行：白色
                    } else {
                        doc.insertString(doc.getLength(), thisLine + "\n", diffLeftStyle); // 左侧不同内容：红色
                    }
                }
                // 右侧文本: 显示 currentContent
                else {
                    if (thisLine.equals(currentLine)) {
                        doc.insertString(doc.getLength(), currentLine + "\n", sameStyle); // 相同行：白色
                    } else {
                        doc.insertString(doc.getLength(), currentLine + "\n", diffRightStyle); // 右侧不同内容：绿色
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        return textPane;
    }

    public boolean hasVersion(String filePath) {
        List<FileVersion> versions = getVersions(filePath);
        return !versions.isEmpty();
    }

    public void removeVersion(String filePath) {
        versionMap.remove(filePath);
        currentVersionMap.remove(filePath); // 移除对应的当前版本号
    }

    public void renameFileVersion(String oldFilePath, String newFilePath, String newFileName) {
        if (versionMap.containsKey(oldFilePath)) {
            List<FileVersion> versions = versionMap.get(oldFilePath);

            for (FileVersion version : versions) {
                version.setFilePath(newFilePath);
                version.setFileName(newFileName);
            }

            versionMap.put(newFilePath, versions);
            versionMap.remove(oldFilePath);
        }
    }
}
