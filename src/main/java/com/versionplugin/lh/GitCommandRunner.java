package com.versionplugin.lh;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class GitCommandRunner {
    public void initializeGitRepo(String repoPath,String baseBranch, String fineGrainedBranch) throws IOException, InterruptedException {

        //检查是否已经是一个Git仓库
        ProcessBuilder checkGitRepo = new ProcessBuilder("git", "rev-parse", "--is-inside-work-tree");
        checkGitRepo.directory(new File(repoPath));
        Process checkProcess = checkGitRepo.start();
        int exitCode = checkProcess.waitFor();

        if (exitCode != 0) {

            //如果不是Git仓库，则初始化
            ProcessBuilder initGit = new ProcessBuilder("git", "init");
            initGit.directory(new File(repoPath));
            Process initProcess = initGit.start();
            int initExitCode = initProcess.waitFor();

            if (initExitCode == 0) {
                System.out.println("Git repository initialized at: " + repoPath);

                //创建并切换到main分支
                ProcessBuilder createMainBranch = new ProcessBuilder("git", "checkout", "-b", baseBranch);
                createMainBranch.directory(new File(repoPath));
                createMainBranch.start().waitFor();

                //创建README文件
                //String filePath = "README.md";
                //ProcessBuilder createREADME =  new ProcessBuilder("powershell", "-Command", "New-Item -Path \"" + filePath + "\" -ItemType \"file\"");
                //createREADME.directory(new File(repoPath));
                //createREADME.start().waitFor();

                //添加README文件到Git索引
                //ProcessBuilder addFile = new ProcessBuilder("git", "add", "README.md");

                //初始化提交
                ProcessBuilder addFile = new ProcessBuilder("git", "add", ".");
                addFile.directory(new File(repoPath));
                addFile.start().waitFor();
                ProcessBuilder commitFile = new ProcessBuilder("git", "commit", "-m", "initial");
                commitFile.directory(new File(repoPath));
                int branchExitCode =commitFile.start().waitFor();

                if (branchExitCode == 0) {
                    System.out.println("Main branch 'main' created and checked out.");
                } else {
                    System.out.println("Failed to create main branch.");
                }
            } else {
                System.out.println("Failed to initialize Git repository.");
            }

            try {

                //创建细粒度更改分支
                createFineGrainedBranch(repoPath, baseBranch, fineGrainedBranch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createFineGrainedBranch(String repoPath, String baseBranch, String fineGrainedBranch) throws IOException, InterruptedException {

        //切换到主分支
        ProcessBuilder checkoutBaseBranch = new ProcessBuilder("git", "checkout", baseBranch);
        checkoutBaseBranch.directory(new File(repoPath));
        Process checkoutBaseProcess = checkoutBaseBranch.start();
        if(checkoutBaseProcess.waitFor()==0){
            System.out.println("切换主分支成功");
        }else{
            System.out.println("切换主分支失败");

        }

        //创建并切换到新的细粒度分支
        ProcessBuilder createBranch = new ProcessBuilder("git", "checkout", "-b", fineGrainedBranch);
        createBranch.directory(new File(repoPath));
        Process createBranchProcess = createBranch.start();
        if(createBranchProcess.waitFor()==0){
            System.out.println("创建细粒度分支成功");
        }else{
            System.out.println("创建细粒度分支失败");

        }

        //初始化提交
        ProcessBuilder addFile = new ProcessBuilder("git", "add", ".");
        addFile.directory(new File(repoPath));
        if(addFile.start().waitFor()==0){
            System.out.println("暂缓成功");
        }else{
            System.out.println("暂缓失败");

        }

        ProcessBuilder commitFile = new ProcessBuilder("git", "commit", "-m", "initial");
        commitFile.directory(new File(repoPath));
        int branchExitCode =commitFile.start().waitFor();
        if (branchExitCode == 0) {
            System.out.println("Main branch 'fineGrainedBranch' created and checked out.");
        } else {
            System.out.println("Failed to create fineGrainedBranch branch.");
        }
    }

    public static boolean isOnFineGrainedBranch(String repoPath) throws IOException, InterruptedException {

        //获取当前分支名
        ProcessBuilder getCurrentBranch = new ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD");
        getCurrentBranch.directory(new File(repoPath));
        Process process = getCurrentBranch.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String currentBranch = reader.readLine(); //读取分支名称
        process.waitFor();

        return "fine-grained-branch".equals(currentBranch);
    }

    public static void commitFineGrainedChanges(String repoPath, String filePath, String fineGrainedBranch,String commitMessage) throws IOException, InterruptedException {

        //添加文件到暂存区
        ProcessBuilder gitAdd = new ProcessBuilder("git", "add", filePath);
        gitAdd.directory(new File(repoPath));
        Process gitAddProcess = gitAdd.start();
        if(gitAddProcess.waitFor()==0){
            System.out.println("添加文件到暂存区");
        }

        //获取状态
        ProcessBuilder gitStatus = new ProcessBuilder("git", "status");
        gitStatus.directory(new File(repoPath));
        Process gitStatusProcess = gitStatus.start();
        BufferedReader statusReader = new BufferedReader(new InputStreamReader(gitStatusProcess.getInputStream()));
        String line;
        while ((line = statusReader.readLine()) != null) {
            System.out.println(line);
        }
        gitStatusProcess.waitFor();

        //提交更改
        ProcessBuilder gitCommit = new ProcessBuilder("git", "commit", "-m", commitMessage);
        gitCommit.directory(new File(repoPath));
        Process gitCommitProcess = gitCommit.start();
        if(gitCommitProcess.waitFor()==0)
        {
            System.out.println("提交更改");
        }

        ProcessBuilder aftergitStatus = new ProcessBuilder("git", "status");
        aftergitStatus.directory(new File(repoPath));
        Process aftergitStatusProcess = aftergitStatus.start();
        BufferedReader afterstatusReader = new BufferedReader(new InputStreamReader(aftergitStatusProcess.getInputStream()));
        String afterline;
        while ((afterline = afterstatusReader.readLine()) != null) {
            System.out.println(afterline);
        }
        aftergitStatusProcess.waitFor();
    }

    public void squashAndMergeFineGrainedCommits(String repoPath, String baseBranch, String fineGrainedBranch, String finalCommitMessage) throws IOException, InterruptedException {

        ProcessBuilder gitStatus = new ProcessBuilder("git", "status");
        gitStatus.directory(new File(repoPath));
        Process gitStatusProcess = gitStatus.start();
        BufferedReader statusReader = new BufferedReader(new InputStreamReader(gitStatusProcess.getInputStream()));
        String line;
        while ((line = statusReader.readLine()) != null) {
            System.out.println(line);
        }
        gitStatusProcess.waitFor();

        //切换到主分支
        ProcessBuilder checkoutBaseBranch = new ProcessBuilder("git", "checkout", baseBranch);
        checkoutBaseBranch.directory(new File(repoPath));
        Process checkoutBaseProcess = checkoutBaseBranch.start();
        if(checkoutBaseProcess.waitFor()==0) {
            System.out.println("切换成功");

        } else{
            System.out.println("切换失败");
        }

        //合并细粒度分支为一个提交
        ProcessBuilder mergeBranch = new ProcessBuilder("git", "merge", "--squash", fineGrainedBranch);
        mergeBranch.directory(new File(repoPath));
        Process mergeBranchProcess = mergeBranch.start();
        if(mergeBranchProcess.waitFor()==0){
            System.out.println("合并成功");

            //提交合并后的更改
            ProcessBuilder gitCommit = new ProcessBuilder("git", "commit", "-m", finalCommitMessage);
            gitCommit.directory(new File(repoPath));
            Process gitCommitProcess = gitCommit.start();
            if(gitCommitProcess.waitFor()==0){
                System.out.println("提交成功");
                JOptionPane.showMessageDialog(null, "Commit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }else{
                System.out.println("提交失败");
            }

        }else{
            JOptionPane.showMessageDialog(null, "合并时发生冲突，请手动解决冲突，然后保存文件！", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /*public void executeFineGrainedCommitMechanism(String repoPath, String filePath, List<String> fineGrainedMessages) throws IOException, InterruptedException {
        String baseBranch = "main";
        String fineGrainedBranch = "fine-grained-branch";

        // 初始化Git仓库（如果需要）
        initializeGitRepo(repoPath);

        // 创建细粒度更改分支
        createFineGrainedBranch(repoPath, baseBranch, fineGrainedBranch);

        // 提交细粒度更改
        for (String message : fineGrainedMessages) {
            commitFineGrainedChanges(repoPath, filePath, message);
        }

        // 合并细粒度更改为一个提交
        squashAndMergeFineGrainedCommits(repoPath, baseBranch, fineGrainedBranch, "Squashed fine-grained changes");
    }*/

}