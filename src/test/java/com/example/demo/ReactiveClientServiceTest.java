package com.example.demo;

import com.example.demo.service.PostProcessingService;
import com.example.demo.service.ReactiveClientService;
import com.example.demo.service.RequestProducer;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
// import های دیگر لازم...
// اضافه کردن Mocking و سایر وابستگی‌ها

class ReactiveClientServiceTest {

    @Test
    void testSubmitAndProcess() {
        // Mocking the dependencies
        RequestProducer mockProducer = mock(RequestProducer.class);
        CuratorFramework mockCurator = mock(CuratorFramework.class);
        PostProcessingService mockPostProcessing = mock(PostProcessingService.class);

        when(mockProducer.createRequest(anyString())).thenReturn(Mono.just("/resources/resource1/request/req-0001"));
        when(mockPostProcessing.processExecution(anyString())).thenReturn(Mono.empty());

        // Creating the service with mocked dependencies
        ReactiveClientService clientService = new ReactiveClientService(mockProducer, mockCurator, /* scheduler */, mockPostProcessing);

        // Testing submit and process method
        clientService.submitAndProcess("resource1")
                .doOnSuccess(unused -> assertTrue(true))
                .doOnError(error -> fail("Should not have thrown an error"))
                .subscribe();

        verify(mockProducer).createRequest("resource1");
        verify(mockPostProcessing).processExecution("/resources/resource1/request/req-0001".replace("/request/", "/execution/"));
    }
}

