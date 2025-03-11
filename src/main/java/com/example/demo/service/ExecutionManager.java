package com.example.demo.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class ExecutionManager {

    private final CuratorFramework curator;
    private final Map<String, NodeCache> monitors = new ConcurrentHashMap<>();

    public ExecutionManager(CuratorFramework curator) {
        this.curator = curator;
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
        // منطق پردازش درخواست‌ها به‌روزرسانی شود.
        System.out.println("Processing requests for: " + basePath);
    }
}


