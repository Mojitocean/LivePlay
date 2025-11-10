package com.mg.core.config;

import com.mg.core.utils.ThreadUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 **/
@Log4j2
@Configuration
public class ThreadPoolConfig {
    /**
     * 核心线程数 = CPU核心数 * 2 (适合计算密集型任务)
     * 如果是IO密集型可以设置为CPU核心数 * (1 + IO等待时间/计算时间)
     */
    private final int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 最大线程数 = 核心线程数 * 2
     */
    private final int maxPoolSize = corePoolSize * 2;
    /**
     * 队列容量 (根据业务需求调整)
     */
    private final int queueCapacity = 1000;

    private ExecutorService taskExecutorService;

    /**
     * 创建任务处理线程池
     *
     * @return 线程池实例
     */
    @Bean(name = "taskExecutorService")
    public ExecutorService taskExecutorService() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS, // 空闲线程存活时间
                new LinkedBlockingQueue<>(queueCapacity),
                new BasicThreadFactory.Builder()
                        .namingPattern("task-pool-%d") // 线程命名模式
                        .daemon(false) // 非守护线程
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy() { // 拒绝策略
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        log.warn("Task {} rejected from {}", r.toString(), e.toString());
                        super.rejectedExecution(r, e);
                    }
                }) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                // 异常处理
                if (t != null) {
                    log.error("Thread pool task error: {}", t.getMessage(), t);
                }
            }
        };
        this.taskExecutorService = executor;
        return executor;
    }

    /**
     * 创建Spring管理的ThreadPoolTaskExecutor (适合Spring异步任务)
     */
    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("controller-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 等待任务完成的最大时间
        executor.initialize();
        return executor;
    }


    /**
     * 销毁事件，关闭定时任务线程池。
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("====关闭后台任务线程池====");
            ThreadUtil.shutdownAndAwaitTermination(taskExecutorService);
        } catch (Exception e) {
            log.error("关闭线程池失败: {}", e.getMessage(), e);
        }
    }
}
