package com.mg.service;



/**
 * packageName com.mg.service
 * Netty 服务，暴露 WebSocket 接口用于控制（start/stop/status）
 * @author mj
 * @className StreamRelayServer
 * @date 2025/11/19
 * @description TODO
 */


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 *
 */
public class StreamRelayServer {
    private final int port;
    private final StreamRelayManager manager;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

    public StreamRelayServer(int port, StreamRelayManager manager) {
        this.port = port;
        this.manager = manager;
    }

    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new StreamRelayInitializer(manager));

        Channel ch = b.bind(port).sync().channel();
        System.out.println("StreamRelayServer started at port " + port);
        ch.closeFuture().sync();
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
