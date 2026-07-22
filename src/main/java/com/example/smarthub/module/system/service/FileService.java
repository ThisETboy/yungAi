package com.example.smarthub.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.module.system.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 上传的文件
     * @param uploadBy 上传人
     * @return 文件信息
     */
    SysFile upload(MultipartFile file, String uploadBy);

    /**
     * 分页查询文件列表
     * @param current 页码
     * @param size 每页大小
     * @param keyword 文件名搜索
     * @return 分页结果
     */
    IPage<SysFile> listFiles(int current, int size, String keyword);

    /**
     * 根据 URL 获取文件物理路径
     */
    String getFileAbsolutePath(String fileUrl);
}
