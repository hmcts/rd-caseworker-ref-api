package uk.gov.hmcts.reform.cwrdapi.controllers.internal;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CaseWorkerInternalApiClient {
    <T> ResponseEntity<Object> postRequest(List<T> requestBody, String path);
}