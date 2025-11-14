package org.example.angelbacked.controller;

import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:/tmp/uploads}")
    private String uploadPath;

    @PostMapping("/image")
    public Result uploadImage(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                              HttpServletRequest request) {
        // 兼容处理，支持 file 或 avatar 参数
        MultipartFile uploadedFile = file != null ? file : avatar;
        
        if (uploadedFile == null) {
            return Result.error("上传文件不能为空");
        }
        
        try {
            // 检查文件是否为空
            if (uploadedFile.isEmpty()) {
                return Result.error("上传文件不能为空");
            }

            // 检查文件类型
            String contentType = uploadedFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只允许上传图片文件");
            }

            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = uploadedFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            File destFile = new File(uploadPath + File.separator + uniqueFilename);
            uploadedFile.transferTo(destFile);

            // 返回文件访问URL
            String fileUrl = "/uploads/" + uniqueFilename;

            return Result.success("上传成功").setData(fileUrl);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("上传异常: " + e.getMessage());
        }
    }
}