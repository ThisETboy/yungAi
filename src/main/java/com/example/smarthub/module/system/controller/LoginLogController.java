package com.example.smarthub.module.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.system.entity.SysLoginLog;
import com.example.smarthub.module.system.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 */
@RestController
@RequestMapping("/api/auth/login-logs")
@RequiredArgsConstructor
@Tag(name = "登录日志")
public class LoginLogController {

    private final SysLoginLogService loginLogService;

    /**
     * 分页查询登录日志
     */
    @GetMapping
    @Operation(summary = "登录日志分页列表")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public R<IPage<SysLoginLog>> page(@RequestParam(defaultValue = "1") int current,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) Integer status) {
        return R.ok(loginLogService.page(current, size, username, status));
    }
}
