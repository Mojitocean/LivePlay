package com.mg.netty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * packageName com.mg.netty
 *
 * @author mj
 * @className NioController
 * @date 2025/11/11
 * @description TODO
 */
@Log4j2
@Tag(name = "NIO学习")
@RestController
@RequestMapping("/nio")
public class NioController {
    //不设置非阻塞NIO会退化为BIO
    @Operation(summary = "NIO非阻塞操作")
    @GetMapping("/hello")
    public Void hello() throws IOException {
        //创建一个ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //创建一个ServerSocket
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8080));
        //设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        //打开selector处理channel
        Selector selector = Selector.open();
        //注册socket channel的连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功...");
        while (true) {
            //阻塞等待需要处理的事件
            selector.select();
            //获取需要处理的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //处理每个事件
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                //如果是OP_ACCEPT事件，则进行连接获取和事件注册
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = server.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("连接成功...");
                } else if (key.isReadable()) {//如果是OP_READ事件，则进行数据读取
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = socketChannel.read(buffer);
                    if (read > 0) {
                        System.out.println("收到客户端数据：" + new String(buffer.array()));
                    } else if (read == -1) {
                        System.out.println("客户端断开连接...");
                        socketChannel.close();
                    }
                }
            }
        }
    }
}