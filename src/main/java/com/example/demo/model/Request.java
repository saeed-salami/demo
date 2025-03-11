package com.example.demo.model;

public class Request {
    private String resourceName;
    private String requestId;

    public Request(String resourceName, String requestId) {
        this.resourceName = resourceName;
        this.requestId = requestId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getRequestId() {
        return requestId;
    }
}

