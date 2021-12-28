package com.thinkdifferent.data.scheduled;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/17 16:56
 */
@Configuration
public class ScheduledConfig {
    @Bean(name = "taskThreadPoolTaskScheduler")
    public TaskScheduler getMyThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        // 可用CPU数 / 2 + 1
        taskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors() / 2 + 1);
        taskScheduler.setThreadNamePrefix("thinkDiff-");
        taskScheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //调度器shutdown被调用时等待当前被调度的任务完成
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setRemoveOnCancelPolicy(true);
        //等待时长
        taskScheduler.setAwaitTerminationSeconds(60);
        return taskScheduler;
    }
}
