package com.example.smarthub.module.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smarthub.common.response.R;
import com.example.smarthub.module.monitor.entity.RequestLog;
import com.example.smarthub.module.monitor.mapper.RequestLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 请求日志控制器
 *
 * 提供请求日志的分页查询接口，用于前端展示和操作审计
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "请求日志")
public class RequestLogController {

    private final RequestLogMapper requestLogMapper;

    /**
     * 分页查询请求日志
     * @param current  页码，默认 1
     * @param size     每页数量，默认 10
     * @param url      可选过滤：按 URL 模糊搜索
     * @param module   可选过滤：按模块筛选
     * @param operator 可选过滤：按操作人筛选
     */
    @GetMapping
    @Operation(summary = "请求日志分页列表")
    @PreAuthorize("hasAuthority('sys:log:list')")
    public R<IPage<RequestLog>> page(@RequestParam(defaultValue = "1") int current,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String url,
                                     @RequestParam(required = false) String module,
                                     @RequestParam(required = false) String operator) {
        Page<RequestLog> page = new Page<>(current, size);
        LambdaQueryWrapper<RequestLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(url != null && !url.isEmpty(), RequestLog::getUrl, url)
               .eq(module != null && !module.isEmpty(), RequestLog::getModule, module)
               .like(operator != null && !operator.isEmpty(), RequestLog::getOperator, operator)
               .orderByDesc(RequestLog::getCreateTime);
        IPage<RequestLog> result = requestLogMapper.selectPage(page, wrapper);
        return R.ok(result);
    }
}
