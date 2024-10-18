package com.versionplugin.lh;

import java.time.LocalDateTime;

public class FileVersion {
    private String filePath;         // 文件路径
    private String content;          // 文件内容
    private LocalDateTime timestamp; // 修改时间
    private String author;           // 修改者

    public FileVersion(String filePath, String content, String author) {
        this.filePath = filePath;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // 设置为当前时间
        this.author = author;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "FileVersion{" +
                "filePath='" + filePath + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", author='" + author + '\'' +
                '}';
    }
}