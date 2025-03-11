package com.example.demo;

import com.example.demo.service.ReactiveClientService;
import com.example.demo.service.ReactiveRequestController;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReactiveRequestControllerTest {

    @Test
    void testSubmitAndProcess() {
        // Mocking the service.
        ReactiveClientService mockClientService = mock(ReactiveClientService.class);

        when(mockClientService.submitAndProcess("resource1")).thenReturn(Mono.empty());

        // Creating the controller with the mocked service.
        ReactiveRequestController controller = new ReactiveRequestController(mockClientService);

        // Testing the submit and process method.
        controller.submitAndProcess("resource1")
                .doOnSuccess(unused -> assertTrue(true))
                .doOnError(error -> fail("Should not have thrown an error"))
                .subscribe();

        verify(mockClientService).submitAndProcess("resource1");
    }
}

