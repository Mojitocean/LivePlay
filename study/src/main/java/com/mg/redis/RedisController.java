package com.mg.redis;


import com.mg.core.domain.R;
import com.mg.redis.utils.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * packageName com.mg.service
 * 1、分布式锁的看门狗机制不要自己指定持持有锁的时间，否则看门狗机制会失效
 * 2、只要方法还在运行，看门狗机制会每隔10S续期一次且一直续期，这就会导致某个人一直持有锁
 * 3、可以强制解锁
 *
 * @author mj
 * @className RedisController
 * @date 2025/11/7
 * @description TODO
 */
@Log4j2
@Tag(name = "Redis学习")
@RestController
@RequestMapping("/redis")
public class RedisController {


    @Operation(summary = "获取锁--指定锁有效期")
    @GetMapping("/getLock")
    public String getLock() throws InterruptedException {
        log.info("线程名称：{}", Thread.currentThread().getName());
        RedisUtil.lock("lock-mj", 20, TimeUnit.SECONDS);
        RLock rLock = RedisUtil.getLock("lock-mj");
        log.info("锁的持有次数1：{}", rLock.getHoldCount());
        log.info("锁的剩余时间1：{}", rLock.remainTimeToLive());
        Thread.sleep(11000L); //睡眠11秒看看看门狗机制会不会失效，不失效这里应该是28S左右
        log.info("锁的持有次数2：{}", rLock.getHoldCount());
        log.info("锁的剩余时间2：{}", rLock.remainTimeToLive());
        Thread.sleep(5000L); //看门狗机制不失效肯定大于20S，按理说大概剩余最多4S
        log.info("锁的持有次数3：{}", rLock.getHoldCount());
        log.info("锁的剩余时间3：{}", rLock.remainTimeToLive());
        return rLock.getName();
    }

    @Operation(summary = "获取锁--看门狗自动续期")
    @GetMapping("/getLockWatchdog")
    public String getLockWatchdog() throws InterruptedException {
        log.info("线程名称：{}", Thread.currentThread().getName());
        RLock rLock = RedisUtil.getLock("lock-mj");
        rLock.lock();
        log.info("锁的持有次数1：{}", rLock.getHoldCount());
        log.info("锁的剩余时间1：{}", rLock.remainTimeToLive());
        Thread.sleep(4000L);
        log.info("锁的持有次数2：{}", rLock.getHoldCount());
        log.info("锁的剩余时间2：{}", rLock.remainTimeToLive());
        Thread.sleep(40000L);
        log.info("锁的持有次数3：{}", rLock.getHoldCount());
        log.info("锁的剩余时间3：{}", rLock.remainTimeToLive());
        return rLock.getName();
    }

    @Operation(summary = "释放锁--强制释放锁")
    @GetMapping("/unLock")
    public String unLock() {
        RLock rLock = RedisUtil.getLock("lock-mj");
        log.info("锁名称：{}", rLock.getName());
        log.info("锁是否被锁：{}", rLock.isLocked());
        log.info("当前线程是否持有锁：{}", rLock.isHeldByCurrentThread());
        log.info("锁的持有次数：{}", rLock.getHoldCount());
        log.info("锁的剩余时间：{}", rLock.remainTimeToLive());
        RedisUtil.forceUnlock(rLock.getName());
        log.info("强制解锁后锁是否被锁：{}", rLock.isLocked());
        return rLock.getName();
    }

    @Operation(summary = "限流方法--滑动窗口")
    @GetMapping("/allow")
    public R<String> allow(@RequestParam String userId) {
        String key = "user:" + userId;
        if (!RedisUtil.allow(key, 5, 50000)) { // 每秒最多 5 次
            return R.error("请求过于频繁，请稍后再试!");
        }
        return R.ok();
    }
}