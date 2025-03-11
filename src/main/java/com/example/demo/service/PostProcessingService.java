package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PostProcessingService {

    public Mono<Void> processExecution(String executionPath) {
        // منطق پردازش پس‌پردازش.
        System.out.println("Processing execution for path: " + executionPath);

        // شبیه‌سازی عملیات ناهمزمان.
        return Mono.fromRunnable(() -> {
            // انجام عملیات سنگین یا ذخیره‌سازی داده‌ها.
            System.out.println("Post-processing completed for: " + executionPath);
        });
    }
}

