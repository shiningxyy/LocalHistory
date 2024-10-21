package com.versionplugin.lh;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GitCommandRunner {
    public void initializeGitRepo(String repoPath) throws IOException, InterruptedException {
        // 检查是否已经是一个Git仓库
        ProcessBuilder checkGitRepo = new ProcessBuilder("git", "rev-parse", "--is-inside-work-tree");
        checkGitRepo.directory(new File(repoPath));
        Process checkProcess = checkGitRepo.start();
        int exitCode = checkProcess.waitFor();

        if (exitCode != 0) {
            // 如果不是Git仓库，则初始化
            ProcessBuilder initGit = new ProcessBuilder("git", "init");
            initGit.directory(new File(repoPath));
            Process initProcess = initGit.start();
            int initExitCode = initProcess.waitFor();

            if (initExitCode == 0) {
                System.out.println("Git repository initialized at: " + repoPath);
            } else {
                System.out.println("Failed to initialize Git repository.");
            }
        }
    }
    public void createFineGrainedBranch(String repoPath, String baseBranch, String fineGrainedBranch) throws IOException, InterruptedException {
        // 切换到主分支
        ProcessBuilder checkoutBaseBranch = new ProcessBuilder("git", "checkout", baseBranch);
        checkoutBaseBranch.directory(new File(repoPath));
        Process checkoutBaseProcess = checkoutBaseBranch.start();
        checkoutBaseProcess.waitFor();

        // 创建并切换到新的细粒度分支
        ProcessBuilder createBranch = new ProcessBuilder("git", "checkout", "-b", fineGrainedBranch);
        createBranch.directory(new File(repoPath));
        Process createBranchProcess = createBranch.start();
        createBranchProcess.waitFor();

        System.out.println("Created and switched to fine-grained branch: " + fineGrainedBranch);
    }
    public void commitFineGrainedChanges(String repoPath, String filePath, String commitMessage) throws IOException, InterruptedException {
        // 添加文件到暂存区
        ProcessBuilder gitAdd = new ProcessBuilder("git", "add", filePath);
        gitAdd.directory(new File(repoPath));
        Process gitAddProcess = gitAdd.start();
        gitAddProcess.waitFor();

        // 提交更改
        ProcessBuilder gitCommit = new ProcessBuilder("git", "commit", "-m", commitMessage);
        gitCommit.directory(new File(repoPath));
        Process gitCommitProcess = gitCommit.start();
        gitCommitProcess.waitFor();

        System.out.println("Committed fine-grained change: " + commitMessage);
    }
    public void squashAndMergeFineGrainedCommits(String repoPath, String baseBranch, String fineGrainedBranch, String finalCommitMessage) throws IOException, InterruptedException {
        // 切换到主分支
        ProcessBuilder checkoutBaseBranch = new ProcessBuilder("git", "checkout", baseBranch);
        checkoutBaseBranch.directory(new File(repoPath));
        Process checkoutBaseProcess = checkoutBaseBranch.start();
        checkoutBaseProcess.waitFor();

        // 合并细粒度分支为一个提交
        ProcessBuilder mergeBranch = new ProcessBuilder("git", "merge", "--squash", fineGrainedBranch);
        mergeBranch.directory(new File(repoPath));
        Process mergeBranchProcess = mergeBranch.start();
        mergeBranchProcess.waitFor();

        // 提交合并后的更改
        ProcessBuilder gitCommit = new ProcessBuilder("git", "commit", "-m", finalCommitMessage);
        gitCommit.directory(new File(repoPath));
        Process gitCommitProcess = gitCommit.start();
        gitCommitProcess.waitFor();

        System.out.println("Squashed and merged changes from " + fineGrainedBranch + " to " + baseBranch);
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