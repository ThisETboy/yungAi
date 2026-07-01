package com.example.smarthub.module.protocol.controller;

import com.example.smarthub.common.response.R;
import com.example.smarthub.module.protocol.service.ProtocolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 协议管理控制器
 *
 * 提供设备通信协议的 REST 接口：
 * - 向设备发送数据
 * - 查询协议状态
 * - 批量启停协议
 */
@RestController
@RequestMapping("/api/protocol")
@RequiredArgsConstructor
@Tag(name = "协议管理")
public class ProtocolController {

    private final ProtocolService protocolService;

    /**
     * 向指定协议的设备发送数据
     * @param protocol  协议名称（mqtt/tcp）
     * @param deviceId  设备ID
     * @param data      数据内容（字符串形式）
     */
    @PostMapping("/send/{protocol}/{deviceId}")
    @Operation(summary = "向设备发送数据")
    @PreAuthorize("hasAuthority('sys:protocol:send')")
    public R<Void> sendData(@PathVariable String protocol,
                             @PathVariable String deviceId,
                             @RequestBody String data) {
        boolean success = protocolService.sendData(protocol, deviceId, data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return success ? R.ok() : R.fail("发送失败");
    }

    /** 获取所有协议的状态 */
    @GetMapping("/status")
    @Operation(summary = "获取所有协议状态")
    @PreAuthorize("hasAuthority('sys:protocol:list')")
    public R<Map<String, Boolean>> status() {
        return R.ok(protocolService.getProtocolStatus());
    }

    /** 启动所有协议适配器 */
    @PostMapping("/start-all")
    @Operation(summary = "启动所有协议")
    @PreAuthorize("hasAuthority('sys:protocol:start')")
    public R<Void> startAll() {
        protocolService.startAllProtocols();
        return R.ok();
    }

    /** 停止所有协议适配器 */
    @PostMapping("/stop-all")
    @Operation(summary = "停止所有协议")
    @PreAuthorize("hasAuthority('sys:protocol:stop')")
    public R<Void> stopAll() {
        protocolService.stopAllProtocols();
        return R.ok();
    }
}
