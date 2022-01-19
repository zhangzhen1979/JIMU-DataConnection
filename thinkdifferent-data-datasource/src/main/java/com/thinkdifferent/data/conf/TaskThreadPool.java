package com.thinkdifferent.data.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class TaskThreadPool {
    @Bean(name = "getAndSavePool")
    public ExecutorService getAndSaveDataPool() {
        return Executors.newCachedThreadPool();
    }
}
