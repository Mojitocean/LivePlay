package com.mg.netty;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * packageName com.mg.netty
 *
 * @author mj
 * @className BioController
 * @date 2025/11/11
 * @description TODO
 */
@Log4j2
@Tag(name = "BIO学习")
@RestController
@RequestMapping("/bio")
public class BioController {

    @Operation(summary = "BIO阻塞操作")
    @GetMapping("/hello")
    public Void hello() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            System.out.println("等待连接...");
            //阻塞方法，有客户端链接才不会阻塞
            Socket clientSocket = serverSocket.accept();
            //进行telnet连接 输入telnet localhost 8080
            System.out.println("连接成功...");
            //进行数据读取
            handle(clientSocket);
        }
    }

    private void handle(Socket clientSocket) throws IOException {
        byte[] bytes = new byte[1024];
        System.out.println("准备read...");
        int read = clientSocket.getInputStream().read(bytes);
        System.out.println("read完毕...");
        //输出获取到的信息
        if (read != -1) {
            System.out.println("接收到客户端消息：" + new String(bytes, 0, read));
        }
        System.out.println("end...");
    }
}