package com.example.demo.service;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class ResourceInitializer {

    private final CuratorFramework curator;

    public ResourceInitializer(CuratorFramework curator) {
        this.curator = curator;
    }

    @PostConstruct
    public void init() throws Exception {
        createResourceStructure("/resources/resource1");
        createResourceStructure("/resources/resource2");
    }

    private void createResourceStructure(String path) throws Exception {
        if (curator.checkExists().forPath(path) == null) {
            curator.create().creatingParentsIfNeeded().forPath(path);
            curator.create().forPath(path + "/request");
            curator.create().forPath(path + "/execution");
        }
    }
}


