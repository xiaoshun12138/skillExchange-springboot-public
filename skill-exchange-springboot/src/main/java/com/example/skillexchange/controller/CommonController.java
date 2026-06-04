package com.example.skillexchange.controller;

import com.example.skillexchange.common.BusinessException;
import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用工具Controller
 * 
 * 接口：POST /api/v1/common/uploadImage
 * 
 * 功能说明：
 * 1. 图片上传功能，供内容发布时使用
 * 2. 图片必须经过微信内容安全审核才能使用
 * 
 * 注意：MVP版本图片存储在本地，后续可切换到云存储
 */
@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    /** 图片上传存储路径，从application.yml读取 */
    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    /** 图片访问URL前缀 */
    @Value("${file.upload.prefix:http://localhost:8080/uploads/}")
    private String uploadPrefix;

    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/uploadImage")
    public R uploadImage(@RequestParam("file") MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            return R.error("请选择要上传的图片");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return R.error("只能上传图片文件");
        }

        // 校验文件大小（限制5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.error("图片大小不能超过5MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 确保上传目录存在
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            File destFile = new File(uploadDir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(destFile);

            // 返回图片访问URL
            String imageUrl = uploadPrefix + newFilename;
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", imageUrl);

            return R.success("上传成功", data);
        } catch (IOException e) {
            throw new BusinessException(500, "图片上传失败，请重试");
        }
    }
}
