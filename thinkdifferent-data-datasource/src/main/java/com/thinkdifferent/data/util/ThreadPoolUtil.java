package com.thinkdifferent.data.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolUtil {
    private static final ThreadPoolTaskExecutor executor;

    static {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() >> 2 + 1);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() >> 1);
        executor.setQueueCapacity(2 << 10);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("tPool-");
        // 线程池对拒绝任务的处理策略 CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
    }

    public static void addTask(Runnable runnable){
        executor.execute(runnable);
    }
}
