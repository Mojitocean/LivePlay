package com.mg.netty;

/**
 * packageName com.mg.netty
 *
 * @author mj
 * @className ServerChannelInitializer
 * @date 2025/11/10
 * @description TODO
 */

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;

/**
 * netty服务端处理器
 **/
@Log4j2
/**
 * netty服务初始化器
 **/
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //添加编解码
        socketChannel.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast(new NettyServerHandler());
    }
}