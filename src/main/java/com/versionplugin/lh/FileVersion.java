package com.versionplugin.lh;

import java.io.Serializable; // 添加导入
import java.time.LocalDateTime;

public class FileVersion implements Serializable { // 实现 Serializable 接口
    private static final long serialVersionUID = 1L; // 添加序列化版本号

    private String filename;         // 文件名
    private String filePath;         // 文件路径
    private String content;          // 文件内容
    private LocalDateTime timestamp; // 修改时间

    public FileVersion(String filename, String filePath, String content) {
        this.filename = filename;
        this.filePath = filePath;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // 设置为当前时间
    }

    public String getFilename() {
        return filename;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FileVersion{" +
                "filename='" + filename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}