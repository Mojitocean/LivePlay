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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        List<SocketChannel> socketChannels = new ArrayList<>();
        //创建一个ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //创建一个ServerSocket
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(8080));
        //设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        System.out.println("服务启动成功...");
        while (true) {
            System.out.println("等待连接...");
            //当设置NIO为非阻塞模式时，accept方法会返回null，否则会阻塞
            //NIO的accept实际上是调用linux的accept函数
            SocketChannel clientSocket = serverSocketChannel.accept();
            if (clientSocket != null) {
                System.out.println("连接成功...");
                clientSocket.configureBlocking(false);
                socketChannels.add(clientSocket);
            }
            //遍历
            Iterator<SocketChannel> iterable = socketChannels.iterator();
            while (iterable.hasNext()) {
                SocketChannel socketChannel = iterable.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int read = socketChannel.read(byteBuffer);
                if (read > 0) {
                    System.out.println("接收到客户端消息：" + new String(byteBuffer.array()));
                } else if (read == -1) {
                    iterable.remove();
                    System.out.println("客户端断开连接...");
                }
            }

        }
    }
}