package com.example.smarthub.module.protocol.adapter.tcp;

import com.example.smarthub.module.protocol.adapter.ProtocolAdapter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCP 协议适配器 — 基于 Netty NIO 服务器
 *
 * 作为 TCP 服务端监听设备连接：
 * - 设备连接后注册到 deviceChannelPool
 * - 通过 channelId 作为设备标识
 * - 支持双向数据收发
 *
 * 新增协议只需要三步：
 * 1. 实现 ProtocolAdapter 接口
 * 2. 标注 @Component（Spring 自动注册到工厂）
 * 3. 在 application.yml 添加配置
 *
 * 其他代码一行都不用改！
 */
@Slf4j
@Component
public class TcpProtocolAdapter implements ProtocolAdapter {

    /** Netty 事件循环组 — Boss 负责接受连接，Worker 负责处理 IO */
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /** 设备通道池 — key=channelId, value=Channel */
    private final Map<String, Channel> deviceChannelPool = new ConcurrentHashMap<>();

    @Value("${protocol.tcp.host:0.0.0.0}")
    private String host;

    @Value("${protocol.tcp.port:9999}")
    private int port;

    @Value("${protocol.tcp.backlog:1024}")
    private int backlog;

    @Value("${protocol.tcp.child.tcp-no-delay:true}")
    private boolean tcpNoDelay;

    @Value("${protocol.tcp.child.keep-alive:true}")
    private boolean keepAlive;

    @Value("${protocol.tcp.max-frame-length:65536}")
    private int maxFrameLength;

    @Override
    public String getProtocolName() {
        return "tcp";
    }

    /**
     * 启动 Netty TCP 服务器
     * 配置：SO_BACKLOG, TCP_NODELAY, SO_KEEPALIVE
     * 管道：StringDecoder → StringEncoder → TcpServerHandler
     */
    @Override
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
                .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 字符串编解码
                        pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                        pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                        // 业务处理器
                        pipeline.addLast(new TcpServerHandler(deviceChannelPool));
                    }
                });

        serverChannel = bootstrap.bind(host, port).sync().channel();
        log.info("TCP server started on {}:{}", host, port);
    }

    /**
     * 停止 TCP 服务器
     * 依次关闭 Channel、BossGroup、WorkerGroup，清空设备池
     */
    @Override
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        deviceChannelPool.clear();
    }

    /**
     * 向设备发送数据
     * @param deviceId 设备 ID（即 Netty channelId）
     * @return true=发送成功, false=通道不存在或不可用
     */
    @Override
    public boolean send(String deviceId, byte[] data) {
        Channel channel = deviceChannelPool.get(deviceId);
        if (channel == null || !channel.isActive()) {
            log.warn("TCP channel not active for device: {}", deviceId);
            return false;
        }
        try {
            channel.writeAndFlush(new String(data));
            return true;
        } catch (Exception e) {
            log.error("TCP send error to device {}: {}", deviceId, e.getMessage());
            deviceChannelPool.remove(deviceId);
            return false;
        }
    }

    /**
     * 检查设备是否在线
     * 从设备通道池中查找并检查通道活跃状态
     */
    @Override
    public boolean isAlive(String deviceId) {
        Channel channel = deviceChannelPool.get(deviceId);
        return channel != null && channel.isActive();
    }

    /** 容器销毁时自动停止 */
    @PreDestroy
    public void destroy() {
        stop();
    }

    /**
     * Netty TCP 服务端处理器
     * 处理设备连接、断开、接收消息等事件
     */
    private static class TcpServerHandler extends ChannelInboundHandlerAdapter {

        private final Map<String, Channel> deviceChannelPool;

        TcpServerHandler(Map<String, Channel> deviceChannelPool) {
            this.deviceChannelPool = deviceChannelPool;
        }

        /** 设备连接 — 注册到设备池 */
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            String channelId = ctx.channel().id().asShortText();
            deviceChannelPool.put(channelId, ctx.channel());
            log.info("TCP device connected: {}, address={}", channelId, ctx.channel().remoteAddress());
        }

        /** 设备断开 — 从设备池移除 */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            String channelId = ctx.channel().id().asShortText();
            deviceChannelPool.remove(channelId);
            log.info("TCP device disconnected: {}", channelId);
        }

        /** 接收设备消息 */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String channelId = ctx.channel().id().asShortText();
            String message = msg.toString();
            log.info("TCP message from device {}: {}", channelId, message);
            // 根据业务需求处理设备消息
            // 例如：解析协议帧、转发到消息队列、存入数据库等
            // 当前实现记录日志，后续可扩展为：
            // - 解析自定义协议帧头/帧尾
            // - 通过 RabbitMQ 转发到消息队列
            // - 调用 ProtocolService 存储到数据库
        }

        /** 捕获异常并关闭连接 */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("TCP error: {}", cause.getMessage(), cause);
            ctx.close();
        }
    }
}
