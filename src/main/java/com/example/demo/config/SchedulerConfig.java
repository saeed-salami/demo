package com.example.demo.config;

import org.apache.curator.shaded.com.google.common.util.concurrent.AbstractScheduledService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerConfig {

    @Bean
    public Scheduler zookeeperScheduler() {
        return Schedulers.newBoundedElastic(
                10,
                100,
                "zk-pool"
        );
    }
}

