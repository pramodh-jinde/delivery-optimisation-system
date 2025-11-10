package com.delivery.routing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfiguration {

    @Value("${routing.optimization.thread-pool-size:20}")
    private int threadPoolSize;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService routingExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

