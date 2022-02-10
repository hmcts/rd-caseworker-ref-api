package uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerInternalApiClientImplTest {
    @Mock
    RestTemplate restTemplate;
    @InjectMocks
    CaseWorkerInternalApiClientImpl caseWorkerInternalApiClient;

    @Test
    void shouldReturnResponseEntityWhenAuthHeadersProvided() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(restTemplate.postForEntity(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        ResponseEntity<Object> responseEntity = caseWorkerInternalApiClient
                .postRequest(Collections.emptyList(), "testPath");
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void shouldReturnResponseEntityWhenNoAuthHeadersProvided() {
        when(restTemplate.postForEntity(any(), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        ResponseEntity<Object> responseEntity = caseWorkerInternalApiClient
                .postRequest(Collections.emptyList(), "testPath");
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

}