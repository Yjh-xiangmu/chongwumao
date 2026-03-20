package com.hajimi.adoption.controller;

import com.hajimi.adoption.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final String UPLOAD_DIR = "E:/Projects/chongwumao/uploads/";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("上传失败，请选择文件");

        // 获取原始文件名和后缀
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一的新文件名，防止覆盖
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            File dest = new File(UPLOAD_DIR + newFileName);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            // 保存文件到本地
            file.transferTo(dest);
            // 返回可直接在浏览器访问的网络 URL
            String fileUrl = "http://localhost:8080/uploads/" + newFileName;
            return Result.success(fileUrl, "上传成功");
        } catch (IOException e) {
            return Result.error("文件上传异常: " + e.getMessage());
        }
    }
}