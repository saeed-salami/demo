package com.example.demo.service;


import com.example.demo.model.Request;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExecutionManager {

    private final CuratorFramework curator;
    private final int cap = 5;
    private final Map<String, NodeCache> monitors = new ConcurrentHashMap<>();
    private final ReactiveClientService reactiveClientService;

    public ExecutionManager(CuratorFramework curator, ReactiveClientService reactiveClientService) {
        this.curator = curator;
        this.reactiveClientService = reactiveClientService;
    }

    @PostConstruct
    public void init() throws Exception {
        List<String> resources = curator.getChildren().forPath("/resources");
        resources.forEach(resource -> startMonitoring("/resources/" + resource + "/request"));
    }

    private void startMonitoring(String path) {
        try {
            NodeCache cache = new NodeCache(curator, path);
            cache.getListenable().addListener(() -> processRequests(path.replace("/request", "")));
            cache.start();
            monitors.put(path, cache);
        } catch (Exception e) {
            throw new RuntimeException("Monitoring failed for " + path, e);
        }
    }

    private void processRequests(String basePath) {
        try {
            List<String> requestNodes = curator.getChildren().forPath(basePath + "/request");

            // فیلتر کردن نودهای execution برای شماردن ظرفیت استفاده شده
            List<String> executionNodes = curator.getChildren().forPath(basePath + "/execution");
            int currentExecutionCount = executionNodes.size();

            int capacity = cap;

            if (currentExecutionCount < capacity && !requestNodes.isEmpty()) {
                // پیدا کردن کوچکترین نود درخواست
                String smallestRequestNode = requestNodes.stream()
                        .min(String::compareTo)
                        .orElse(null);

                if (smallestRequestNode != null) {
                    String resourceName = basePath.substring(basePath.lastIndexOf("/") + 1);
                    String requestId = new String(curator.getData().forPath(basePath + "/request/" + smallestRequestNode), StandardCharsets.UTF_8);
                    Request request = new Request(resourceName, requestId);
                    reactiveClientService.moveToExecution(request);
                    System.out.println("Moved request {} to execution for resource {}" + smallestRequestNode + resourceName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing requests for: " + basePath + " - " + e.getMessage());
        }
    }

    //تابع بررسی خالی شدن نود اگزکیوشن و انتقال به درخواست
    public void onExecutionNodeRemoved(String resourceName){
        try{
            String basePath = "/resources/" + resourceName;
            List<String> requestNodes = curator.getChildren().forPath(basePath + "/request");
            List<String> executionNodes = curator.getChildren().forPath(basePath + "/execution");
            int capacity = cap;

            if (executionNodes.size() < capacity && !requestNodes.isEmpty()) {
                // پیدا کردن کوچکترین نود درخواست
                String smallestRequestNode = requestNodes.stream()
                        .min(String::compareTo)
                        .orElse(null);

                if (smallestRequestNode != null) {
                    String requestId = new String(curator.getData().forPath(basePath + "/request/" + smallestRequestNode), StandardCharsets.UTF_8);
                    Request request = new Request(resourceName, requestId);
                    reactiveClientService.moveToExecution(request);
                    System.out.println("Moved request {} to execution for resource {}" + smallestRequestNode + resourceName);
                }
            }

        }catch (Exception e){
            System.err.println("Error on Execution Node Removed" + resourceName + " - " + e.getMessage());
        }

    }
}



