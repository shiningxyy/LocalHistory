package com.versionplugin.lh;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class MyPlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 使用 CommonDataKeys 获取当前选中的文件
        VirtualFile selectedFile = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());

        if (selectedFile != null) {
            String filePath = selectedFile.getPath();
            // 在这里添加你对文件的处理逻辑
            System.out.println("Selected file: " + filePath);
        } else {
            System.out.println("No file selected.");
        }
    }
}
