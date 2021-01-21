package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CaseWorkerServiceFacade {
    ResponseEntity<Object> processFile(MultipartFile file);
}
