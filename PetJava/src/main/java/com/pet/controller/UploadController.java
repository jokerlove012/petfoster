package com.pet.controller;

import com.pet.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件上传接口
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/api/uploads}")
    private String baseUrl;

    private Path absoluteUploadPath;

    @PostConstruct
    public void init() {
        // 使用项目根目录下的uploads文件夹
        absoluteUploadPath = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        log.info("上传目录: {}", absoluteUploadPath);
        try {
            Files.createDirectories(absoluteUploadPath);
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
        }
    }

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 按日期分目录
            String dateDir = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String relativePath = dateDir + "/" + filename;

            // 创建目录
            Path dirPath = absoluteUploadPath.resolve(dateDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 保存文件
            Path filePath = absoluteUploadPath.resolve(relativePath);
            Files.copy(file.getInputStream(), filePath);

            // 返回URL
            Map<String, String> result = new HashMap<>();
            result.put("url", baseUrl + "/" + relativePath);
            result.put("filename", filename);
            result.put("originalFilename", originalFilename);

            log.info("文件上传成功: {}", filePath);
            return Result.success(result);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/multiple")
    public Result<List<Map<String, String>>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }

        List<Map<String, String>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Result<Map<String, String>> result = upload(file);
                if (result.getCode() == 200) {
                    results.add(result.getData());
                }
            }
        }

        return Result.success(results);
    }
}
