package com.mg.service;

import lombok.SneakyThrows;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className Play
 * @date 2025/11/19
 * @description TODO
 */
public class Play {

    @SneakyThrows
    public void play() {
        // 初始化流管理器（传入 redisson）
        StreamRelayManager manager = new StreamRelayManager(16); // 最大并发16路
        // 启动 Netty 控制服务
        StreamRelayServer server = new StreamRelayServer(8080, manager);
        server.start();
    }
}