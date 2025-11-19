package com.mg.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className StreamRelayHandler
 * @date 2025/11/19
 * @description TODO
 */
public class StreamRelayHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final StreamRelayManager manager;

    public StreamRelayHandler(StreamRelayManager manager) {
        this.manager = manager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String text = msg.text();
        // 简单解析：例如 {"action":"start","cameraId":"cam1","rtsp":"rtsp://...","stream":"stream1"}
        Map<String, String> map = SimpleJson.parse(text);
        String action = map.get("action");
        String cameraId = map.get("cameraId");

        try {
            if ("start".equalsIgnoreCase(action)) {
                String rtsp = map.get("rtsp");
                String stream = map.get("stream");
                // 启动推流任务（异步）
                manager.startRelay(cameraId, rtsp, stream).thenAccept(result -> {
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(result));
                });
            } else if ("stop".equalsIgnoreCase(action)) {
                manager.stopRelay(cameraId);
                ctx.channel().writeAndFlush(new TextWebSocketFrame("stopped"));
            } else if ("status".equalsIgnoreCase(action)) {
                String status = manager.status(cameraId);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(status));
            } else {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("unknown action"));
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("error:" + e.getMessage()));
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}