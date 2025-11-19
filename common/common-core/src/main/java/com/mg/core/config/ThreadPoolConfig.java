package com.mg.core.config;


import com.mg.core.utils.ThreadUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池配置类
 * <p>
 * 功能说明：
 * 1. 提供多种类型的线程池，适用于不同业务场景
 * 2. 支持配置化参数，便于不同环境调整
 * 3. 内置监控和优雅关闭机制
 * 4. 支持上下文传递和异常处理
 *
 * @author Your Name
 * @version 1.0
 */
@Log4j2
@EnableAsync // 启用Spring异步执行支持，使@Async注解生效
@Configuration // 标识为配置类，Spring启动时会自动加载
@ConfigurationProperties(prefix = "thread-pool") // 将配置文件中的thread-pool前缀属性绑定到本类字段
public class ThreadPoolConfig {

    // ==================== 线程池核心参数配置 ====================

    /**
     * 核心线程数 - 线程池中保持活跃的最小线程数量
     * 默认值：CPU核心数（适合计算密集型任务）
     * 对于IO密集型任务，建议设置为CPU核心数 * 2 ~ 4
     */
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * 最大线程数 - 线程池允许创建的最大线程数量
     * 默认值：CPU核心数 * 2
     * 当队列满且核心线程都在忙时，会创建新线程直到达到此限制
     */
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 队列容量 - 用于存放等待执行任务的阻塞队列大小
     * 默认值：1024
     * 注意：队列过大可能消耗大量内存，过小可能导致频繁触发拒绝策略
     */
    private int queueCapacity = 1024;

    /**
     * 线程空闲时间 - 非核心线程空闲时的存活时间（秒）
     * 默认值：30秒
     * 超过此时间且线程数大于corePoolSize时，空闲线程会被回收
     */
    private long keepAliveTime = 30L;

    /**
     * 是否预启动所有核心线程 - 线程池创建时是否立即创建所有核心线程
     * 默认值：false
     * 设置为true可减少首次任务执行的延迟，但会增加启动时的资源消耗
     */
    private boolean preStartAllCoreThreads = false;


    // Bean实例引用，用于优雅关闭
    private ExecutorService taskExecutorService;

    // ==================== 自定义线程工厂实现 ====================

    /**
     * 自定义线程工厂类
     * <p>
     * 功能特点：
     * 1. 自定义线程命名，便于监控和调试
     * 2. 支持设置守护线程和优先级
     * 3. 统一的未捕获异常处理
     * 4. 线程编号自动递增
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1); // 线程编号，保证线程名唯一
        private final String namePrefix;      // 线程名前缀
        private final boolean daemon;         // 是否为守护线程
        private final int priority;           // 线程优先级

        /**
         * 全参数构造函数
         *
         * @param namePrefix 线程名前缀，如"task-executor-"
         * @param daemon     是否为守护线程
         * @param priority   线程优先级，使用Thread常量如Thread.NORM_PRIORITY
         */
        public CustomThreadFactory(String namePrefix, boolean daemon, int priority) {
            this.namePrefix = namePrefix + "-";
            this.daemon = daemon;
            this.priority = priority;
        }

        /**
         * 简化构造函数，使用默认参数
         * 默认：非守护线程，正常优先级
         */
        public CustomThreadFactory(String namePrefix) {
            this(namePrefix, false, Thread.NORM_PRIORITY);
        }

        /**
         * 创建新线程
         *
         * @param r 要执行的任务
         * @return 配置好的Thread实例
         */
        @Override
        public Thread newThread(Runnable r) {
            // 创建线程，名称格式：namePrefix-1, namePrefix-2, ...
            Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            thread.setDaemon(daemon);         // 设置守护线程属性
            thread.setPriority(priority);     // 设置线程优先级

            // 设置未捕获异常处理器，避免异常丢失
            thread.setUncaughtExceptionHandler((t, e) -> {
                log.error("Uncaught exception in thread: {}", t.getName(), e);
            });

            return thread;
        }
    }

    // ==================== 线程池Bean定义 ====================

    /**
     * 创建通用任务处理线程池 (JDK原生ThreadPoolExecutor)
     * <p>
     * 适用场景：
     * - 普通的异步任务处理
     * - 不需要Spring事务管理的任务
     * - 需要精细控制线程池行为的场景
     * <p>
     * 特点：
     * - 使用LinkedBlockingQueue作为工作队列
     * - 自定义拒绝策略和线程工厂
     * - 支持任务执行前后钩子
     * - 自动关闭（通过destroyMethod）
     *
     * @return ExecutorService 线程池实例
     */
    @Bean(name = "taskExecutorService", destroyMethod = "shutdown")
    public ExecutorService taskExecutorService() {
        // 创建ThreadPoolExecutor实例
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,      // 核心线程数
                maxPoolSize,       // 最大线程数
                keepAliveTime,     // 空闲线程存活时间
                TimeUnit.SECONDS,  // 时间单位
                new LinkedBlockingQueue<>(queueCapacity), // 有界阻塞队列
                new CustomThreadFactory("task-executor", false, Thread.NORM_PRIORITY), // 线程工厂
                new LoggingCallerRunsPolicy() // 拒绝策略
        ) {
            /**
             * 任务执行前钩子方法
             * 在任务执行前被调用，可以用于记录日志、设置上下文等
             *
             * @param t 执行任务的线程
             * @param r 要执行的任务
             */
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                // 调试级别日志，记录任务开始执行
                if (log.isDebugEnabled()) {
                    log.debug("Thread [{}] start executing task: {}", t.getName(), r.getClass().getSimpleName());
                }
            }

            /**
             * 任务执行后钩子方法
             * 在任务执行完成后被调用，可以用于异常处理、资源清理等
             *
             * @param r 已完成的任务
             * @param t 任务执行过程中的异常，正常完成时为null
             */
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                // 统一处理执行结果和异常
                handleExecutionResult(r, t);
            }

            /**
             * 线程池终止钩子方法
             * 在线程池完全终止时被调用，可以用于资源清理等
             */
            @Override
            protected void terminated() {
                super.terminated();
                log.info("ThreadPool [taskExecutorService] terminated");
            }
        };

        // 预启动所有核心线程（可选）
        if (preStartAllCoreThreads) {
            executor.prestartAllCoreThreads();
            log.info("Pre-started all core threads: {}", corePoolSize);
        }

        // 注册线程池监控
        registerThreadPoolMetrics(executor);

        // 保存引用用于优雅关闭
        this.taskExecutorService = executor;
        return executor;
    }

    /**
     * 创建Spring管理的异步任务执行器 (ThreadPoolTaskExecutor)
     * <p>
     * 适用场景：
     * - 使用@Async注解的异步方法
     * - 需要Spring事务管理的异步任务
     * - 与Spring框架深度集成的场景
     * <p>
     * 特点：
     * - Spring框架封装的线程池
     * - 支持任务装饰器（用于上下文传递）
     * - 优雅关闭支持
     * - 与@Async注解无缝集成
     *
     * @return ThreadPoolTaskExecutor Spring线程池实例
     */
    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 基础参数配置
        executor.setCorePoolSize(corePoolSize);           // 核心线程数
        executor.setMaxPoolSize(maxPoolSize);             // 最大线程数
        executor.setQueueCapacity(queueCapacity);         // 队列容量
        executor.setKeepAliveSeconds((int) keepAliveTime); // 线程空闲时间
        executor.setThreadNamePrefix("async-task-");      // 线程名前缀

        // 拒绝策略 - 调用者运行策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 优雅关闭配置
        executor.setWaitForTasksToCompleteOnShutdown(true); // 等待任务完成
        executor.setAwaitTerminationSeconds(60);          // 最大等待时间

        // 任务装饰器 - 用于MDC上下文传递等
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 线程配置
        executor.setThreadPriority(Thread.NORM_PRIORITY); // 线程优先级
        executor.setAllowCoreThreadTimeOut(false);        // 核心线程不超时

        // 使用自定义线程工厂
        executor.setThreadFactory(new CustomThreadFactory("async-task"));

        // 初始化线程池
        executor.initialize();

        log.info("AsyncTaskExecutor initialized: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    /**
     * 创建IO密集型任务线程池
     * <p>
     * 适用场景：
     * - 高IO等待的任务（如网络请求、文件操作、数据库查询等）
     * - 任务执行时间大部分在等待IO操作完成的场景
     * <p>
     * 特点：
     * - 更大的线程数配置（CPU核心数 * 4 ~ 8）
     * - 允许核心线程超时，避免长期空闲占用资源
     * - 使用独立的线程命名，便于监控
     *
     * @return ExecutorService IO密集型任务线程池
     */
    @Bean(name = "ioIntensiveExecutor")
    public ExecutorService ioIntensiveExecutor() {
        // IO密集型任务可以设置更多的线程，因为线程大部分时间在等待IO
        int ioCorePoolSize = Runtime.getRuntime().availableProcessors() * 4;
        int ioMaxPoolSize = Runtime.getRuntime().availableProcessors() * 8;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                ioCorePoolSize,    // 较大的核心线程数
                ioMaxPoolSize,     // 更大的最大线程数
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new CustomThreadFactory("io-executor"),
                new LoggingCallerRunsPolicy());

        // 允许核心线程超时，在长时间空闲时回收核心线程以节省资源
        executor.allowCoreThreadTimeOut(true);

        return executor;
    }

    /**
     * 创建定时任务线程池
     * <p>
     * 适用场景：
     * - 需要定时执行或延迟执行的任务
     * - 周期性任务调度
     * <p>
     * 特点：
     * - 基于ScheduledThreadPoolExecutor
     * - 支持定时执行和周期性执行
     * - 核心线程数至少为2，确保基本调度能力
     *
     * @return ScheduledExecutorService 定时任务线程池
     */
    @Bean(name = "scheduledTaskExecutor")
    public ScheduledExecutorService scheduledTaskExecutor() {
        return new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors()), // 至少2个核心线程
                new CustomThreadFactory("scheduled-task"),
                new LoggingCallerRunsPolicy());
    }

    // ==================== 内部辅助类和工具方法 ====================

    /**
     * 自定义拒绝策略 - 增强的调用者运行策略
     * <p>
     * 功能特点：
     * 1. 在触发拒绝策略时记录详细的线程池状态
     * 2. 保持调用者运行策略的行为（由调用线程直接执行任务）
     * 3. 提供监控和告警依据
     */
    private static class LoggingCallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {
        /**
         * 拒绝任务时的处理逻辑
         *
         * @param r 被拒绝的任务
         * @param e 线程池实例
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            // 记录详细的线程池状态，便于问题排查
            log.warn("Task rejected from thread pool '{}', active threads: {}, pool size: {}, queue size: {}, completed tasks: {}",
                    e.toString(),
                    e.getActiveCount(),    // 活跃线程数
                    e.getPoolSize(),       // 当前线程数
                    e.getQueue().size(),   // 队列中任务数
                    e.getCompletedTaskCount()); // 已完成任务数

            // 调用父类逻辑：由调用线程直接执行该任务
            super.rejectedExecution(r, e);
        }
    }

    /**
     * MDC任务装饰器 - 用于线程间上下文传递
     * <p>
     * 功能说明：
     * 1. 在执行任务前保存当前线程的上下文（如MDC、SecurityContext等）
     * 2. 在新线程中恢复上下文
     * 3. 确保任务执行后清理上下文，避免内存泄漏
     * <p>
     * 使用场景：
     * - 日志MDC上下文传递
     * - Spring Security上下文传递
     * - 自定义业务上下文传递
     */
    private static class MdcTaskDecorator implements TaskDecorator {
        /**
         * 装饰任务，添加上下文传递逻辑
         *
         * @param runnable 原始任务
         * @return 包装后的任务
         */
        @NotNull
        @Override
        public Runnable decorate(@NotNull Runnable runnable) {
            // 在这里保存当前线程的上下文信息
            // 例如：Map<String, String> originalContext = MDC.getCopyOfContextMap();

            return () -> {
                try {
                    // 在新线程中恢复上下文
                    // 例如：if (originalContext != null) { MDC.setContextMap(originalContext); }

                    // 执行原始任务
                    runnable.run();
                } finally {
                    // 确保执行后清理上下文，避免内存泄漏和上下文污染
                    // 例如：MDC.clear();
                }
            };
        }
    }

    /**
     * 统一的执行结果处理方法
     * <p>
     * 功能说明：
     * 1. 统一处理任务执行过程中的异常
     * 2. 记录任务执行成功日志（调试级别）
     * 3. 可扩展添加监控指标上报、告警等逻辑
     *
     * @param r 执行的任务
     * @param t 执行过程中的异常，正常完成时为null
     */
    private void handleExecutionResult(Runnable r, Throwable t) {
        if (t != null) {
            // 异常处理：记录错误日志
            log.error("Thread pool task execution failed, task: {}", r.getClass().getSimpleName(), t);

            // 这里可以扩展功能：
            // 1. 上报监控指标
            // 2. 发送告警通知
            // 3. 记录错误统计等
        } else if (log.isDebugEnabled()) {
            // 调试日志：记录任务成功完成
            log.debug("Thread pool task completed successfully: {}", r.getClass().getSimpleName());
        }
    }

    /**
     * 注册线程池监控
     * <p>
     * 功能说明：
     * 1. 定期输出线程池状态日志
     * 2. 监控线程池健康状态
     * 3. 可扩展集成Micrometer等监控框架
     *
     * @param executor 要监控的线程池
     */
    private void registerThreadPoolMetrics(ThreadPoolExecutor executor) {
        // 仅在INFO级别日志开启时注册监控，避免不必要的性能开销
        if (log.isInfoEnabled()) {
            // 创建单线程调度器用于监控
            ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(
                    new CustomThreadFactory("threadpool-monitor"));

            // 定时执行监控任务：初始延迟1分钟，每5分钟执行一次
            monitor.scheduleAtFixedRate(() -> {
                log.info("ThreadPool '{}' stats - Active: {}, Pool: {}, Core: {}, Queue: {}/{}, Completed: {}, TaskCount: {}",
                        "taskExecutorService",
                        executor.getActiveCount(),        // 活跃线程数
                        executor.getPoolSize(),           // 当前线程数
                        executor.getCorePoolSize(),       // 核心线程数
                        executor.getQueue().size(),       // 队列当前大小
                        executor.getQueue().remainingCapacity() + executor.getQueue().size(), // 队列总容量
                        executor.getCompletedTaskCount(), // 已完成任务数
                        executor.getTaskCount());         // 总任务数
            }, 1, 5, TimeUnit.MINUTES);

            // 注意：这个监控调度器需要在实际项目中妥善管理，避免内存泄漏
            // 在生产环境中建议使用专业的监控框架如Micrometer
        }
    }

    // ==================== 生命周期管理 ====================

    /**
     * 优雅关闭方法
     * <p>
     * 执行时机：Spring容器关闭时，在Bean销毁前执行
     * <p>
     * 功能说明：
     * 1. 平滑关闭线程池，等待正在执行的任务完成
     * 2. 限制最大等待时间，避免无限期等待
     * 3. 记录关闭过程日志，便于问题排查
     */
    @PreDestroy
    public void destroy() {
        log.info("Shutting down thread pools...");

        // 关闭通用任务线程池
        if (taskExecutorService != null) {
            try {
                ThreadUtil.shutdownAndAwaitTermination(taskExecutorService);
                log.info("TaskExecutorService shutdown completed");
            } catch (Exception e) {
                log.error("Failed to shutdown TaskExecutorService: {}", e.getMessage(), e);
            }
        }

        // 注意：Spring管理的ThreadPoolTaskExecutor会自动由Spring容器关闭
        // 其他线程池也需要在这里添加关闭逻辑
    }
}

/*
 # 线程池配置
 thread-pool:
 core-pool-size: 8           # 核心线程数，默认CPU核心数
 max-pool-size: 16           # 最大线程数，默认CPU核心数*2
 queue-capacity: 1024        # 队列容量，默认1024
 keep-alive-time: 30         # 线程空闲时间(秒)，默认30
 pre-start-all-core-threads: false  # 是否预启动核心线程，默认false

 # Spring任务执行配置（与@Async注解配合使用）
 spring:
 task:
 execution:
 pool:
 core-size: ${thread-pool.core-pool-size}     # 核心线程数
 max-size: ${thread-pool.max-pool-size}       # 最大线程数
 queue-capacity: ${thread-pool.queue-capacity} # 队列容量
 keep-alive: ${thread-pool.keep-alive-time}s  # 空闲时间
 */