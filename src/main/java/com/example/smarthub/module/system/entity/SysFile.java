package com.example.smarthub.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体 — 对应 sys_file 表
 */
@Data
@TableName("sys_file")
@Schema(description = "文件实体")
public class SysFile {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件类型(MIME)")
    private String fileType;

    @Schema(description = "文件扩展名")
    private String fileExt;

    @Schema(description = "存储物理路径")
    private String storagePath;

    @Schema(description = "上传人")
    private String uploadBy;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
