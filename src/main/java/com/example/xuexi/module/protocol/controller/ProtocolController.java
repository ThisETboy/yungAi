package com.example.xuexi.module.protocol.controller;

import com.example.xuexi.common.response.R;
import com.example.xuexi.module.protocol.service.ProtocolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/protocol")
@RequiredArgsConstructor
@Tag(name = "协议管理")
public class ProtocolController {

    private final ProtocolService protocolService;

    @PostMapping("/send/{protocol}/{deviceId}")
    @Operation(summary = "向设备发送数据")
    public R<Void> sendData(@PathVariable String protocol,
                             @PathVariable String deviceId,
                             @RequestBody byte[] data) {
        protocolService.sendData(protocol, deviceId, data);
        return R.ok();
    }

    @GetMapping("/status")
    @Operation(summary = "获取所有协议状态")
    public R<Map<String, Boolean>> status() {
        return R.ok(protocolService.getProtocolStatus());
    }

    @PostMapping("/start-all")
    @Operation(summary = "启动所有协议")
    public R<Void> startAll() {
        protocolService.startAllProtocols();
        return R.ok();
    }

    @PostMapping("/stop-all")
    @Operation(summary = "停止所有协议")
    public R<Void> stopAll() {
        protocolService.stopAllProtocols();
        return R.ok();
    }
}
