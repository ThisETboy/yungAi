package com.example.smarthub.module.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.ai.entity.AiModelConfig;
import com.example.smarthub.module.ai.service.AiModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI 模型配置控制器
 *
 * 提供 AI 模型配置的 CRUD 接口
 */
@RestController
@RequestMapping("/api/ai/models")
@RequiredArgsConstructor
@Tag(name = "AI模型配置")
public class AiModelConfigController {

    private final AiModelConfigService modelConfigService;

    /**
     * 分页查询 AI 模型配置
     */
    @GetMapping
    @Operation(summary = "AI模型配置分页列表")
    @PreAuthorize("hasAuthority('sys:ai:model')")
    public R<IPage<AiModelConfig>> page(@RequestParam(defaultValue = "1") int current,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String provider) {
        return R.ok(modelConfigService.page(current, size, provider));
    }

    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "AI模型配置详情")
    @PreAuthorize("hasAuthority('sys:ai:model')")
    public R<AiModelConfig> getById(@PathVariable Long id) {
        return R.ok(modelConfigService.getById(id));
    }

    /**
     * 创建或更新 AI 模型配置
     */
    @PostMapping
    @Operation(summary = "创建/更新AI模型配置")
    @PreAuthorize("hasAuthority('sys:ai:model')")
    public R<Void> save(@RequestBody AiModelConfig config) {
        modelConfigService.saveOrUpdate(config);
        return R.ok();
    }

    /**
     * 删除 AI 模型配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除AI模型配置")
    @PreAuthorize("hasAuthority('sys:ai:model')")
    public R<Void> delete(@PathVariable Long id) {
        modelConfigService.deleteById(id);
        return R.ok();
    }
}
