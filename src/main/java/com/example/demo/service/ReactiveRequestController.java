package com.example.demo.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactiveRequestController {

    private final ReactiveClientService clientService;

    public ReactiveRequestController(ReactiveClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * متدی برای ارسال درخواست و پردازش آن
     *
     * @param resourceName نام منبعی که باید پردازش شود
     * @return یک Mono که نشان‌دهنده تکمیل عملیات است
     */
    public Mono<Void> submitAndProcess(String resourceName) {
        return clientService.submitAndProcess(resourceName)
                .doOnSuccess(unused -> System.out.println("Processing completed for resource: " + resourceName))
                .doOnError(error -> System.err.println("Error processing resource: " + resourceName + ", Error: " + error.getMessage()));
    }
}
