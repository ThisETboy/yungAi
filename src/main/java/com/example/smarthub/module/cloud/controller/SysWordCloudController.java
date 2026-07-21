package com.example.smarthub.module.cloud.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.cloud.dto.WordCloudRequest;
import com.example.smarthub.module.cloud.service.SysWordCloudService;
import com.example.smarthub.module.cloud.vo.WordCloudVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 词云管理控制器
 */
@RestController
@RequestMapping("/api/cloud")
@RequiredArgsConstructor
@Tag(name = "词云中心")
public class SysWordCloudController {

    private final SysWordCloudService sysWordCloudService;

    /**
     * 获取词云数据（用于前端渲染）
     */
    @GetMapping("/words")
    @Operation(summary = "获取词云数据")
    @PreAuthorize("hasAuthority('sys:cloud:list')")
    public R<List<WordCloudVO>> getWordCloudList(@RequestParam(required = false) String category) {
        return R.ok(sysWordCloudService.getWordCloudList(category));
    }

    /**
     * 获取所有分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取分类列表")
    @PreAuthorize("hasAuthority('sys:cloud:list')")
    public R<List<String>> getCategories() {
        return R.ok(sysWordCloudService.getCategories());
    }

    /**
     * 分页查询热词列表
     */
    @GetMapping("/words/page")
    @Operation(summary = "分页查询热词")
    @PreAuthorize("hasAuthority('sys:cloud:list')")
    public R<IPage<WordCloudVO>> pageWords(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return R.ok(sysWordCloudService.pageWords(current, size, category, keyword));
    }

    /**
     * 新增或编辑热词
     */
    @PostMapping("/words")
    @Operation(summary = "新增/编辑热词")
    @PreAuthorize("hasAuthority('sys:cloud:add') or hasAuthority('sys:cloud:edit')")
    @OperateLog(title = "词云热词", businessType = OperateLog.BusinessType.UPDATE)
    public R<Void> saveWord(@Valid @RequestBody WordCloudRequest request) {
        sysWordCloudService.saveWord(request);
        return R.ok();
    }

    /**
     * 删除热词
     */
    @DeleteMapping("/words/{id}")
    @Operation(summary = "删除热词")
    @PreAuthorize("hasAuthority('sys:cloud:delete')")
    @OperateLog(title = "词云热词", businessType = OperateLog.BusinessType.DELETE)
    public R<Void> deleteWord(@PathVariable Long id) {
        sysWordCloudService.deleteWord(id);
        return R.ok();
    }
}
