package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CaseWorkerServiceFacade {
    /**
     * To process CaseWorker file.
     * @param file file
     * @return ResponseEntity
     */
    ResponseEntity<Object> processFile(MultipartFile file);
}
