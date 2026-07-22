package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.SysFile;
import com.example.smarthub.module.system.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 文件管理控制器
 *
 * 提供文件上传、下载、预览接口
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理")
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("hasAuthority('sys:file:upload')")
    @OperateLog(title = "文件管理", businessType = BusinessType.INSERT)
    public R<SysFile> upload(@RequestParam("file") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.isAuthenticated() ? auth.getName() : "system";
        SysFile sysFile = fileService.upload(file, username);
        return R.ok(sysFile);
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "文件列表")
    @PreAuthorize("hasAuthority('sys:file:list')")
    public R<IPage<SysFile>> listFiles(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(fileService.listFiles(current, size, keyword));
    }

    /**
     * 下载/预览文件
     */
    @GetMapping(value = "/**", produces = "application/octet-stream")
    @Operation(summary = "下载/预览文件")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String fileRelativePath = uri.substring(contextPath.length() + "/api/files".length() + 1);
        String fileUrl = "/api/files/" + fileRelativePath;

        String absPath = fileService.getFileAbsolutePath(fileUrl);
        File file = new File(absPath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("文件不存在");
            return;
        }

        String contentType = java.nio.file.Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";
        response.setContentType(contentType);

        String fileName = file.getName();
        response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLengthLong(file.length());

        try (InputStream is = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
}
