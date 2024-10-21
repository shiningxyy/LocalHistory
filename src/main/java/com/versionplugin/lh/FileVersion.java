package com.versionplugin.lh;

import java.time.LocalDateTime;

public class FileVersion {
    private String filename;         // 文件名
    private String filePath;         // 文件路径
    private String content;          // 文件内容
    private LocalDateTime timestamp; // 修改时间
    private int versionNum;
    public FileVersion(String filename,String filePath, String content,int versionNum) {
        this.filename = filename;
        this.filePath = filePath;
        this.content = content;
        this.versionNum=versionNum;
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

    public void setVersionNum(int VersionNum){
        this.versionNum=versionNum;
    }

    public int getVersionNum(){
        return versionNum;
    }

    @Override
    public String toString() {
        return "FileVersion{"+
                "filename='" + filename + '\'' +
                "filePath='" + filePath + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp+
                '}';
    }
}