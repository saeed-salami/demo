package com.example.demo.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ReactiveClientService {

    private final RequestProducer producer;
    private final CuratorFramework curator;
    private final Scheduler scheduler;
    private final PostProcessingService postProcessingService;

    public ReactiveClientService(
            RequestProducer producer,
            CuratorFramework curator,
            @Qualifier("zookeeperScheduler") Scheduler scheduler,
            PostProcessingService postProcessingService
    ) {
        this.producer = producer;
        this.curator = curator;
        this.scheduler = scheduler;
        this.postProcessingService = postProcessingService;
    }

    public Mono<Void> submitAndProcess(String resourceName) {
        return producer.createRequest(resourceName)
                .flatMap(this::monitorExecution) // monitorExecution باید Mono<String> برگرداند
                .flatMap(postProcessingService::processExecution) // processExecution باید Mono<Void> برگرداند
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(30));
    }

    private Mono<String> monitorExecution(String requestPath) {
        return Mono.create(sink -> {
            String executionPath = requestPath.replace("/request/", "/execution/");
            NodeCache cache = new NodeCache(curator, executionPath);

            try {
                cache.start();
                cache.getListenable().addListener(() -> {
                    if (cache.getCurrentData() != null) {
                        sink.success(executionPath); // مطمئن شوید که executionPath یک String است
                        cache.close();
                    }
                });
            } catch (Exception e) {
                sink.error(e);
            }
        }).subscribeOn(scheduler);
    }
}


