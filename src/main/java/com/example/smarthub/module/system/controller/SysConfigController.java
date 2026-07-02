package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.annotation.OperateLog;
import com.example.smarthub.common.annotation.OperateLog.BusinessType;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.SysConfig;
import com.example.smarthub.module.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "系统配置")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 分页查询系统配置
     */
    @GetMapping
    @Operation(summary = "系统配置分页列表")
    @PreAuthorize("hasAuthority('sys:config:list')")
    public R<IPage<SysConfig>> page(@RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String configName) {
        return R.ok(sysConfigService.page(current, size, configName));
    }

    /**
     * 根据键获取配置值
     */
    @GetMapping("/key/{configKey}")
    @Operation(summary = "根据键获取配置值")
    public R<String> getConfigByKey(@PathVariable String configKey) {
        return R.ok(sysConfigService.getConfigByKey(configKey));
    }

    /**
     * 根据 ID 查询
     */
    @GetMapping("/{id}")
    @Operation(summary = "系统配置详情")
    @PreAuthorize("hasAuthority('sys:config:list')")
    public R<SysConfig> getById(@PathVariable Long id) {
        return R.ok(sysConfigService.getById(id));
    }

    /**
     * 创建或更新配置
     */
    @PostMapping
    @Operation(summary = "创建/更新系统配置")
    @PreAuthorize("hasAuthority('sys:config:edit')")
    @OperateLog(title = "系统配置", businessType = BusinessType.UPDATE)
    public R<Void> save(@Valid @RequestBody SysConfig config) {
        sysConfigService.saveOrUpdate(config);
        return R.ok();
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统配置")
    @PreAuthorize("hasAuthority('sys:config:delete')")
    @OperateLog(title = "系统配置", businessType = BusinessType.DELETE)
    public R<Void> delete(@PathVariable Long id) {
        sysConfigService.deleteById(id);
        return R.ok();
    }
}
