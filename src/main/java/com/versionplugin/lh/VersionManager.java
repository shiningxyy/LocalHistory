package com.versionplugin.lh;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;

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
    public Set<String> getFilenames() {
        return versionMap.keySet();
    }



    public String rollbackVersion(String filepath,int number){
        List<FileVersion> versions = getVersions(filepath);
        FileVersion rollbackVer=versions.get(number);
        String rollbackCon= rollbackVer.getContent();

        System.out.println("回滚内容：\n"+rollbackCon);
        try {
            // 将新内容写入文件，覆盖原有内容
            Files.write(Paths.get(filepath), rollbackCon.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("文件内容已回滚到版本 " + number);

            //String currentContent = new String(Files.readAllBytes(Paths.get(filepath)));
            //System.out.println("当前文件内容: \n" + currentContent);
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
        if (!versions.isEmpty()) {
            return true;
        }
        return false;
    }

    public void removeVersion(String filePath) {
        versionMap.remove(filePath);
    }
}
