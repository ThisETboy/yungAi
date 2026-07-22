package com.example.smarthub.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.common.exception.BizException;
import com.example.smarthub.module.system.entity.SysFile;
import com.example.smarthub.module.system.mapper.SysFileMapper;
import com.example.smarthub.module.system.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件服务实现 — 本地磁盘存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final SysFileMapper sysFileMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.prefix:/api/files}")
    private String accessPrefix;

    @Override
    public SysFile upload(MultipartFile file, String uploadBy) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        // 使用绝对路径
        String absUploadPath = new File(uploadPath).getAbsolutePath();

        // 创建存储目录
        String dateDir = java.time.LocalDate.now().toString().replace("-", "/");
        String dirPath = absUploadPath + "/" + dateDir;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String filePath = dirPath + "/" + newFileName;
        String fileUrl = accessPrefix + "/" + dateDir + "/" + newFileName;

        try {
            File destFile = new File(filePath);
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
        }

        // 保存文件信息到数据库
        SysFile sysFile = new SysFile();
        sysFile.setFileName(originalFilename != null ? originalFilename : newFileName);
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(file.getContentType());
        sysFile.setFileExt(ext);
        sysFile.setStoragePath(filePath);
        sysFile.setUploadBy(uploadBy != null ? uploadBy : "system");
        sysFile.setRemark("");
        sysFileMapper.insert(sysFile);

        return sysFile;
    }

    @Override
    public IPage<SysFile> listFiles(int current, int size, String keyword) {
        Page<SysFile> page = new Page<>(current, size);
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null && !keyword.isEmpty(), SysFile::getFileName, keyword)
               .orderByDesc(SysFile::getCreateTime);
        return sysFileMapper.selectPage(page, wrapper);
    }

    @Override
    public String getFileAbsolutePath(String fileUrl) {
        // /api/files/2026/07/01/xxxx.jpg -> uploads/2026/07/01/xxxx.jpg
        String relativePath = fileUrl.replace(accessPrefix, "").substring(1);
        return new File(uploadPath).getAbsolutePath() + "/" + relativePath;
    }
}
