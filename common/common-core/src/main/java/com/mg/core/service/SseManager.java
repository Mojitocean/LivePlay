package com.mg.core.service;


import com.mg.core.utils.ServletUtil;
import com.mg.core.utils.StringUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;

/**
 * packageName com.mg.core.service
 * SSE工具类
 *
 * @author mj
 * @className SseManager
 * @date 2025/9/4
 * @description TODO
 */
@Log4j2
@Component
public class SseManager {

    /**
     * 默认超时时间（0 表示不过期，浏览器端断开才会失效）
     */
    private static final Long DEFAULT_TIMEOUT = 0L;
    private static final String DEFAULT_TOPIC = "SYS_MSG_TOPIC";
    private static final long HEARTBEAT_INTERVAL = 15;

    /**
     * clientId -> SseEmitter
     */
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();
    /**
     * clientId -> topic
     */
    private final Map<String, String> clientTopics = new ConcurrentHashMap<>();

    @Resource(name = "taskExecutorService")
    private ExecutorService executor;

    /**
     * 创建连接
     *
     * @param clientId 客户端 ID（建议用用户ID/UUID） topic 默认为 SYS_MSG_TOPIC
     * @return SseEmitter
     */
    public SseEmitter createConnection(String clientId, String topic) {
        if (topic == null || topic.isBlank()) {
            topic = DEFAULT_TOPIC;
        }
        // 创建 SseEmitter
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        clients.put(clientId, emitter);
        clientTopics.put(clientId, topic);
        // 连接完成/超时/异常时清理
        emitter.onCompletion(() -> {
            removeClient(clientId);
        });
        emitter.onTimeout(() -> {
            log.warn("客户端 {} 连接超时", clientId);
            removeClient(clientId);
        });
        emitter.onError(e -> {
            log.error("客户端 {} 连接异常: {}", clientId, e.getMessage());
            removeClient(clientId);
        });
        log.info("客户端 {} 已连接，topic={}", clientId, topic);
        return emitter;
    }

    /**
     * 单播
     */
    public void sendToClient(String clientId, String message) {
        if (StringUtil.isBlank(clientId)) {
            clientId = StringUtil.substring(ServletUtil.getCurrentHttpRequestHeader("authorization"), 7);
        }
        SseEmitter emitter = clients.get(clientId);
        if (emitter == null) {
            log.warn("客户端 {} 不存在", clientId);
            return;
        }
        asyncSend(emitter, "message", message, clientId);
    }

    /**
     * 分组广播
     */
    public void sendToTopic(String topic, String message) {
        log.info("向 topic={} 广播消息: {}", topic, message);
        clientTopics.forEach((clientId, t) -> {
            if (t.equals(topic)) {
                sendToClient(clientId, message);
            }
        });
    }

    /**
     * 全局广播
     */
    public void broadcast(String message) {
        log.info("全局广播消息: {}", message);
        clients.keySet().forEach(clientId -> sendToClient(clientId, message));
    }

    /**
     * 移除客户端
     */
    public void removeClient(String clientId) {
        clients.remove(clientId);
        clientTopics.remove(clientId);
        log.info("客户端 {} 已移除", clientId);
    }

    /**
     * 异步发送
     */
    private void asyncSend(SseEmitter emitter, String event, String data, String clientId) {
        executor.submit(() -> {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
                log.info("向客户端 {} 发送消息成功: {}", clientId, data);
            } catch (IOException e) {
                log.error("发送失败，移除客户端 {}: {}", clientId, e.getMessage());
                removeClient(clientId);
            }
        });
    }

    /**
     * 定时心跳
     */
    @PostConstruct
    private void startHeartbeatTask() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            log.debug("执行 SSE 心跳检测");
            clients.forEach((clientId, emitter) ->
                    asyncSend(emitter, "ping", "keep-alive " + LocalDateTime.now(), clientId));
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }
}