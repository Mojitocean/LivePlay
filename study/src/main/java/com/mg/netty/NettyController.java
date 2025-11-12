package com.mg.netty;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName com.mg.netty
 *
 * @author mj
 * @className NettyController
 * @date 2025/11/12
 * @description TODO
 */
@Log4j2
@Tag(name = "Netty学习")
@RestController
@RequestMapping("/netty")
public class NettyController {

    @Operation(summary = "NIO非阻塞操作")
    @GetMapping("/start")
    public void start() {
        EventLoop eventLoop = new NioEventLoopGroup().next();

    }
}