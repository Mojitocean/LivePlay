package com.mg.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className StreamRelayInitializer
 * @date 2025/11/19
 * @description TODO
 */
public class StreamRelayInitializer extends ChannelInitializer<Channel> {
    private final StreamRelayManager manager;

    public StreamRelayInitializer(StreamRelayManager manager) {
        this.manager = manager;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new WebSocketServerProtocolHandler("/ws"));
        p.addLast(new StreamRelayHandler(manager));
    }
}