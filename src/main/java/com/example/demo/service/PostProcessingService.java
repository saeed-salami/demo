package com.example.demo.service;

import com.example.demo.model.Request;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PostProcessingService {

    private final CuratorFramework curator;
    private final ExecutionManager executionManager;

    public PostProcessingService(CuratorFramework curator, ExecutionManager executionManager) {
        this.curator = curator;
        this.executionManager = executionManager;
    }

    public Mono<Void> processExecution(Request request) {
        String executionPath = String.format("/resources/%s/execution/%s", request.getResourceName(), request.getRequestId());
        System.out.println("Processing execution for path: " + executionPath);
        return Mono.fromRunnable(() -> {
            try {
                // انجام عملیات سنگین یا ذخیره‌سازی داده‌ها.
                System.out.println("Post-processing completed for: " + executionPath);
                // حذف نود execution پس از پردازش
                curator.delete().forPath(executionPath);
                executionManager.onExecutionNodeRemoved(request.getResourceName());
            } catch (Exception e) {
                System.err.println("Error deleting execution node: " + e.getMessage());
            }
        });
    }
}



