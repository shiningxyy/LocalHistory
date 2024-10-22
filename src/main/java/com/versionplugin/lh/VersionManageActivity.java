package com.versionplugin.lh;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VersionManageActivity implements StartupActivity {
    private static VersionManager versionManager;
    private static GitCommandRunner gitCommandRunner; // 使用 GitCommandRunner
    public VersionManageActivity() {
        this.versionManager = new VersionManager();
        this.gitCommandRunner = new GitCommandRunner();
    }

    // 初始化文件版本
    public void initializeFileVersions(Project project) {
        // 加载版本数据
        versionManager.loadFromFile(Paths.get(project.getBasePath(), "version_data.ser").toString());

        List<VirtualFile> files = getAllFiles(project); // 获取当前项目中的所有文件
        for (VirtualFile file : files) {
            String fileName = file.getName(); // 获取文件名
            String filePath = file.getPath(); // 获取文件路径
            if (fileName.startsWith(".") || filePath.contains(".git")) {
                continue; // 跳过当前文件，继续下一个文件
            }
            // 检查文件是否已经存在版本
            if (!versionManager.hasVersion(filePath)) {
                // 获取文件内容
                String initialContent = "";

                try {
                    initialContent = new String(file.contentsToByteArray()); // 使用 VirtualFile 获取文件内容
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                versionManager.initializeFileVersion(fileName, filePath, initialContent); // 创建初始版本
            }
        }
        // 初始化Git仓库
        String baseBranch = "main";
        String fineGrainedBranch = "fine-grained-branch";
        String repoPath = project.getBasePath();

        try {
            gitCommandRunner.initializeGitRepo(repoPath,baseBranch,fineGrainedBranch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 获取当前项目的所有文件名
    public static List<String> getAllFileNames(Project project) {
        List<String> fileNames = new ArrayList<>();

        // 获取项目的根目录
        VirtualFile[] projectRoots = ProjectRootManager.getInstance(project).getContentRoots();

        for (VirtualFile root : projectRoots) {
            // 遍历项目根目录下的所有文件和文件夹
            visitFilesNameRecursively(root, fileNames);
        }

        return fileNames;
    }

    // 递归遍历文件夹并添加文件名到列表
    private static void visitFilesNameRecursively(@NotNull VirtualFile root, List<String> fileNames) {
        root.refresh(false, true);  // 确保文件系统是最新的

        VirtualFileVisitor<Void> visitor = new VirtualFileVisitor<>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                // 如果是文件而不是目录，获取文件名并添加到列表
                if (!file.isDirectory()) {
                    fileNames.add(file.getPath()); // 使用文件的完整路径
                }
                return true; // 继续遍历
            }
        };

        // 使用传统的 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesNameRecursively(child, fileNames); // 递归遍历子目录
            } else {
                visitor.visitFile(child); // 访问文件
            }
        }
    }

    // 获取当前项目的所有文件路径
    public static List<String> getAllFilePaths(Project project) {
        Set<String> keySet = versionManager.getFilenames();
        List<String> filePaths = new ArrayList<>(keySet); // 将 Set 转换为 List

        return filePaths;
    }

    // 递归遍历文件夹并添加文件路径到列表
    private static void visitFilesPathRecursively(@NotNull VirtualFile root, List<String> filePaths) {
        root.refresh(false, true);  // 确保文件系统是最新的

        // 使用 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesPathRecursively(child, filePaths); // 递归遍历子目录
            } else {
                filePaths.add(child.getPath()); // 添加文件路径到列表
            }
        }
    }

    // 获取当前项目的所有 VirtualFile
    public static List<VirtualFile> getAllFiles(Project project) {
        List<VirtualFile> files = new ArrayList<>();

        // 获取项目的根目录
        VirtualFile[] projectRoots = ProjectRootManager.getInstance(project).getContentRoots();

        for (VirtualFile root : projectRoots) {
            // 遍历项目根目录下的所有文件和文件夹
            visitFilesRecursively(root, files);
        }

        return files;
    }

    // 递归遍历文件夹并添加文件到列表
    private static void visitFilesRecursively(@NotNull VirtualFile root, List<VirtualFile> files) {
        root.refresh(false, true);  // 确保文件系统是最新的

        // 使用 for 循环遍历子文件
        VirtualFile[] children = root.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                visitFilesRecursively(child, files); // 递归遍历子目录
            } else {
                files.add(child); // 添加文件到列表
            }
        }
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }
    public GitCommandRunner getGitCommandRunner() {
        return gitCommandRunner;
    }
    @Override
    public void runActivity(@NotNull Project project) {
        registerSaveListener(project); // Register the save listener
        registerBulkDeleteListener(project);
        registerBulkRenameListener(project);
    }
    private void registerSaveListener(Project project) {
        ApplicationManager.getApplication().getMessageBus().connect(project)
                .subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerListener() {
                    @Override
                    public void beforeDocumentSaving(@NotNull Document document) {
                        // 获取文件路径
                        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                        if (virtualFile != null) {
                            String fileName=virtualFile.getName();
                            String filePath = virtualFile.getPath();
                            System.out.println("File being saved: " + filePath);

                            System.out.println("启用监听器");
                            try {
                                if(!gitCommandRunner.isOnFineGrainedBranch(project.getBasePath())){
                                    ProcessBuilder gitStatus = new ProcessBuilder("git", "status");
                                    gitStatus.directory(new File(project.getBasePath()));
                                    Process gitStatusProcess = gitStatus.start();
                                    BufferedReader statusReader = new BufferedReader(new InputStreamReader(gitStatusProcess.getInputStream()));
                                    String line;
                                    while ((line = statusReader.readLine()) != null) {
                                        System.out.println(line);
                                    }
                                    gitStatusProcess.waitFor();
                                    // 切换到细粒度分支

                                    ProcessBuilder checkoutBaseBranch = new ProcessBuilder("git", "checkout", "fine-grained-branch");
                                    checkoutBaseBranch.directory(new File(project.getBasePath()));
                                    Process checkoutBaseProcess = checkoutBaseBranch.start();
                                    if(checkoutBaseProcess.waitFor()==0) {
                                        System.out.println("切换成功");
                                    } else{
                                        System.out.println("切换失败");
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            ApplicationManager.getApplication().invokeLater(() -> {
                                try {
                                    // 获取文件内容并添加版本
                                    String newContent = document.getText();
                                    versionManager.addVersion(filePath, new FileVersion(fileName, filePath, newContent));
                                    versionManager.saveToFile(Paths.get(project.getBasePath(), "version_data.ser").toString());
                                    gitCommandRunner.commitFineGrainedChanges(project.getBasePath(), filePath, "fine-grained-branch","commit：" + fileName + " version time: " + versionManager.getLatestVersion(filePath).getTimestamp());
                                    System.out.println("保存后执行操作完成: " + filePath);
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });

                        } else {
                            System.out.println("Unable to determine file path. Virtual file is null.");
                        }
                    }
                });
    }

    public void registerBulkDeleteListener(Project project) {
        ApplicationManager.getApplication().getMessageBus().connect(project)
                .subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
                    @Override
                    public void before(@NotNull List<? extends VFileEvent> events) {
                        for (VFileEvent event : events) {
                            if (event instanceof VFileDeleteEvent) {
                                VirtualFile file = event.getFile();
                                if (file != null && event.isValid()) {
                                    String filePath = file.getPath();
                                    String fileName = file.getName();

                                    // 自定义删除前操作
                                    System.out.println("File is being deleted: " + filePath);

                                    // 你可以选择在文件删除之前保存版本或执行其他操作
                                    if (versionManager.hasVersion(filePath)) {
                                        versionManager.removeVersion(filePath);
                                        System.out.println("Removed versions for file: " + filePath);
                                        System.out.println("文件名: " + fileName + ", 版本数量: " + versionManager.getVersions(filePath).size());
                                    } else {
                                        System.out.println("No versions found for file: " + filePath);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void after(@NotNull List<? extends VFileEvent> events) {
                        for (VFileEvent event : events) {
                            if (event instanceof VFileDeleteEvent) {
                                VirtualFile file = event.getFile();
                                if (file != null && event.isValid()) {
                                    String filePath = file.getPath();
                                    System.out.println("File deleted: " + filePath);
                                    // 在此处执行其他你想在文件删除后执行的操作
                                }
                            }
                        }
                    }
                });
    }
    public void registerBulkRenameListener(Project project) {
        ApplicationManager.getApplication().getMessageBus().connect(project)
                .subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
                    @Override
                    public void before(@NotNull List<? extends VFileEvent> events) {

                    }

                    @Override
                    public void after(@NotNull List<? extends VFileEvent> events) {
                        for (VFileEvent event : events) {
                            if (event instanceof VFilePropertyChangeEvent) {
                                VFilePropertyChangeEvent propertyChangeEvent = (VFilePropertyChangeEvent) event;
                                String propertyName = propertyChangeEvent.getPropertyName();

                                // 检测是否为重命名事件
                                if (VirtualFile.PROP_NAME.equals(propertyName)) {
                                    VirtualFile file = propertyChangeEvent.getFile();
                                    String oldFilePath = file.getParent().getPath() + "/" + propertyChangeEvent.getOldValue();
                                    String newFilePath = file.getPath();
                                    String newFileName = file.getName();

                                    System.out.println("File renamed from: " + oldFilePath + " to: " + newFilePath);

                                    // 更新版本管理中的路径和文件名
                                    if (versionManager.hasVersion(oldFilePath)) {
                                        versionManager.renameFileVersion(oldFilePath, newFilePath, newFileName);
                                        System.out.println("Updated version manager for renamed file: " + newFilePath);
                                    } else {
                                        System.out.println("No versions found for file: " + oldFilePath);
                                    }
                                }
                            }
                        }
                    }
                });
    }

}
