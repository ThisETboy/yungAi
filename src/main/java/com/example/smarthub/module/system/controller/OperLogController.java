package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.OperLog;
import com.example.smarthub.module.system.mapper.OperLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/oper-log")
@RequiredArgsConstructor
@Tag(name = "操作日志")
public class OperLogController {

    private final OperLogMapper operLogMapper;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    @Operation(summary = "操作日志分页列表")
    @PreAuthorize("hasAuthority('sys:operlog:list')")
    public R<IPage<OperLog>> page(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String title,
                                  @RequestParam(required = false) String operName) {
        Page<OperLog> page = new Page<>(current, size);
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(title != null && !title.isEmpty(), OperLog::getTitle, title)
               .like(operName != null && !operName.isEmpty(), OperLog::getOperName, operName)
               .orderByDesc(OperLog::getOperTime);
        return R.ok(operLogMapper.selectPage(page, wrapper));
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除操作日志")
    @PreAuthorize("hasAuthority('sys:operlog:delete')")
    public R<Void> delete(@PathVariable Long id) {
        operLogMapper.deleteById(id);
        return R.ok();
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清空操作日志")
    @PreAuthorize("hasAuthority('sys:operlog:delete')")
    @OperateLog(title = "操作日志", businessType = BusinessType.DELETE)
    public R<Void> clean() {
        operLogMapper.delete(null);
        return R.ok();
    }
}
