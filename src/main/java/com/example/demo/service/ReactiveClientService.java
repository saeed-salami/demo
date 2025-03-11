package com.example.demo.service;

import com.example.demo.model.Request;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
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
    private final ExecutionManager executionManager;

    public ReactiveClientService(
            RequestProducer producer,
            CuratorFramework curator,
            @Qualifier("zookeeperScheduler") Scheduler scheduler,
            PostProcessingService postProcessingService,
            ExecutionManager executionManager
    ) {
        this.producer = producer;
        this.curator = curator;
        this.scheduler = scheduler;
        this.postProcessingService = postProcessingService;
        this.executionManager = executionManager;
    }

    public Mono<Void> submitAndProcess(Request request) {
        return producer.createRequest(request)
                .flatMap(requestPath -> moveToExecution(request))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(30));
    }

    public Mono<Void> moveToExecution(Request request) {
        String requestPath = String.format("/resources/%s/request/%s", request.getResourceName(), request.getRequestId());
        String executionPath = String.format("/resources/%s/execution/%s", request.getResourceName(), request.getRequestId());

        return Mono.fromCallable(() -> {
                    try {
                        // انتقال داده‌ها از request به execution
                        byte[] data = curator.getData().forPath(requestPath);
                        curator.create().forPath(executionPath, data);

                        // حذف نود request
                        curator.delete().forPath(requestPath);

                        return request;
                    } catch (KeeperException.NoNodeException e) {
                        System.err.println("No node exists at path: " + requestPath + " - " + e.getMessage());
                        return null; // یا throw e; اگر می‌خواهید خطا به بالا برود
                    } catch (Exception e) {
                        System.err.println("Error moving node to execution: " + e.getMessage());
                        return null; // یا throw e; اگر می‌خواهید خطا به بالا برود
                    }
                })
                .flatMap(req -> {
                    if (req != null) {
                        return Mono.just(req);
                    } else {
                        return Mono.empty(); // یا Mono.error(new SomeException());
                    }
                })
                .flatMap(postProcessingService::processExecution)
                .subscribeOn(scheduler)
                .then();
    }

}


