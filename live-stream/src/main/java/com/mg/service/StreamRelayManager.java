package com.mg.service;

import com.mg.redis.utils.RedisUtil;
import org.redisson.api.RBucket;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * packageName com.mg.service
 *
 * @author mj
 * @className StreamRelayManager
 * @date 2025/11/19
 * @description TODO
 */
public class StreamRelayManager {
    private final ExecutorService relayExecutor;
    private final ConcurrentHashMap<String, Future<String>> cameraTaskMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> cameraToChannel = new ConcurrentHashMap<>();
    private final int maxConcurrency;
    private final AtomicInteger currentCount = new AtomicInteger(0);

    // 通道前缀与数量（示例）
    private final String channelPrefix = "live_ch"; // 实际使用请按项目调整
    private final String channelKeyPrefix = "camera:live_"; // redis key 前缀
    private final int totalChannels = 16; // 总通道数
    private final long channelExpireSeconds = 60; // 通道过期时间

    public StreamRelayManager(int maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
        this.relayExecutor = Executors.newFixedThreadPool(maxConcurrency, new ThreadFactory() {
            private final AtomicInteger idx = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "relay-exec-" + idx.incrementAndGet());
                t.setDaemon(false);
                return t;
            }
        });
    }

    /**
     * 启动推流：返回 CompletableFuture 在任务完成或失败时回调
     */
    public CompletableFuture<String> startRelay(String cameraId, String rtspUrl, String streamName) {
        return CompletableFuture.supplyAsync(() -> {
            // 防止超出并发数
            if (currentCount.incrementAndGet() > maxConcurrency) {
                currentCount.decrementAndGet();
                throw new RuntimeException("超出最大并发推流数");
            }

            try {
                // 1) 原子分配通道（Redisson trySet）
                String channelKey = allocateChannel(cameraId);
                if (channelKey == null) {
                    throw new RuntimeException("没有可用通道");
                }

                // 2) 生成 rtmp 地址（根据 channelKey 生成 stream 名称）
                String rtmpUrl = "rtmp://127.0.0.1:1935/live/" + streamName; // 示例

                // 3) 提交推流任务到线程池（返回 Future）
                FFmpegRelayTask task = new FFmpegRelayTask(rtspUrl, rtmpUrl, channelKey, cameraId);
                Future<String> f = relayExecutor.submit(task);
                cameraTaskMap.put(cameraId, f);
                cameraToChannel.put(cameraId, channelKey);

                // 4) 等待短期结果返回（这里直接返回任务提交成功的提示）
                return "started:" + channelKey + ", rtmp=" + rtmpUrl;
            } catch (Exception e) {
                currentCount.decrementAndGet();
                throw new RuntimeException(e.getMessage(), e);
            }
        }, relayExecutor);
    }

    /**
     * 停止推流任务
     */
    public void stopRelay(String cameraId) {
        Future<String> f = cameraTaskMap.remove(cameraId);
        String channelKey = cameraToChannel.remove(cameraId);
        if (f != null) {
            f.cancel(true); // 尝试中断任务
        }
        // 清理 Redis 键
        if (channelKey != null) {
            RedisUtil.deleteObject(channelKey);
            RedisUtil.deleteObject("camera:map:" + cameraId);

        }
        currentCount.decrementAndGet();
    }

    /**
     * 查询任务状态
     */
    public String status(String cameraId) {
        Future<String> f = cameraTaskMap.get(cameraId);
        if (f == null) return "stopped";
        if (f.isCancelled()) return "cancelled";
        if (f.isDone()) {
            try {
                return "done:" + f.get();
            } catch (Exception e) {
                return "done:error:" + e.getMessage();
            }
        }
        return "running";
    }

    /**
     * 原子分配通道（尝试使用 Redisson RBucket.trySet）
     */
    private String allocateChannel(String cameraId) {
        for (int i = 1; i <= totalChannels; i++) {
            String channelKey = channelKeyPrefix + i; // eg: camera:live_1
            RBucket<String> bucket = RedisUtil.getCacheObject(channelKey);
            boolean ok = bucket.trySet(cameraId, channelExpireSeconds, TimeUnit.SECONDS);
            if (ok) {
                // 写反向映射，便于根据 cameraId 快速释放
                RBucket<String> bucket1 = RedisUtil.getCacheObject("camera:map:" + cameraId);
                bucket1.set(channelKey, channelExpireSeconds, TimeUnit.SECONDS);
                return channelKey;
            }
        }
        return null;
    }
}