package com.versionplugin.lh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitCommandRunner {
    public static void main(String[] args) {
        try {
            // 创建ProcessBuilder对象，并设置要执行的命令
            ProcessBuilder processBuilder = new ProcessBuilder("git", "status");

            // 设置工作目录，如果需要在特定目录下执行Git命令
            // processBuilder.directory(new File("path/to/git/repository"));

            // 启动进程
            Process process = processBuilder.start();

            // 获取命令执行的输出流
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 读取输出
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("Command executed with exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}