package com.example.demo.service;


import com.example.demo.model.Request;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Service
public class RequestProducer {

    private final CuratorFramework curator;
    private final Scheduler scheduler;

    public RequestProducer(CuratorFramework curator, @Qualifier("zookeeperScheduler") Scheduler scheduler) {
        this.curator = curator;
        this.scheduler = scheduler;
    }

    public Mono<String> createRequest(Request request) {
        return Mono.fromCallable(() -> {
                    String path = String.format("/resources/%s/request/%s", request.getResourceName(), request.getRequestId());
                    return curator.create()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(path, request.getRequestId().getBytes()); // ذخیره requestId به عنوان داده نود
                })
                .subscribeOn(scheduler);
    }
}




