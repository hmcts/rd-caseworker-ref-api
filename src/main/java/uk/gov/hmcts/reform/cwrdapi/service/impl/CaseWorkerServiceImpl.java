package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

@Service
@Slf4j
@Setter
public class CaseWorkerServiceImpl implements CaseWorkerService {


    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Transactional
    public void createOrganisationFrom(CaseWorkersProfileCreationRequest caseWorkerCreationRequest) {


    }
}

