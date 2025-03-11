package com.example.demo;

import com.example.demo.model.Request;
import com.example.demo.service.PostProcessingService;
import com.example.demo.service.ReactiveClientService;
import com.example.demo.service.RequestProducer;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReactiveClientServiceTest {

    @Mock
    private RequestProducer mockProducer;

    @Mock
    private CuratorFramework mockCurator;

    @Mock
    private PostProcessingService mockPostProcessing;

    @Mock
    private Scheduler mockScheduler;

    @InjectMocks
    private ReactiveClientService clientService;

    @Test
    void testSubmitAndProcess() throws Exception {
        // Given
        Request request = new Request("resource1", "test-request-id");
        when(mockProducer.createRequest(request)).thenReturn(Mono.just("/resources/resource1/request/test-request-id"));
        when(mockPostProcessing.processExecution(request)).thenReturn(Mono.empty());
        when(mockCurator.getData().forPath("/resources/resource1/request/test-request-id")).thenReturn("test-request-id".getBytes());

        // When
        clientService.submitAndProcess(request)
                .doOnSuccess(unused -> {
                    // Then
                    verify(mockProducer).createRequest(request);
                    verify(mockPostProcessing).processExecution(request);
                    try {
                        verify(mockCurator).delete().forPath("/resources/resource1/request/test-request-id");
                        verify(mockCurator).create().forPath("/resources/resource1/execution/test-request-id", "test-request-id".getBytes());
                    } catch (Exception e) {
                        fail("Exception during verification: " + e.getMessage());
                    }
                })
                .doOnError(error -> fail("Should not have thrown an error"))
                .subscribe();
    }
}


