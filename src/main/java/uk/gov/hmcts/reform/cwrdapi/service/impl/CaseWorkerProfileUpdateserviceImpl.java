package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkersProfileUpdationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileUpdateservice;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorStaffProfile;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.Optional;

import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;


@Service
public class CaseWorkerProfileUpdateserviceImpl implements CaseWorkerProfileUpdateservice {

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    IJsrValidatorStaffProfile jsrValidatorStaffProfile;

    @Override
    @Transactional
    public CaseWorkersProfileUpdationResponse updateCaseWorkerProfile(
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest) {

        jsrValidatorStaffProfile
            .validateCaseWorkerUpdateRequest(caseWorkersProfileUpdationRequest,STAFF_PROFILE_CREATE);

        Optional<CaseWorkerProfile> caseWorkerProfile = caseWorkerProfileRepo
            .findByCaseWorkerId(caseWorkersProfileUpdationRequest.getUserId());
        if (!caseWorkerProfile.isPresent()) {
            throw new ResourceNotFoundException(CaseWorkerConstants.NO_DATA_FOUND);
        }

        CaseWorkerProfile cwProfile = caseWorkerProfile.get();
        cwProfile.setCaseWorkerId(caseWorkersProfileUpdationRequest.getUserId());
        cwProfile.setFirstName(caseWorkersProfileUpdationRequest.getFirstName());
        cwProfile.setLastName(caseWorkersProfileUpdationRequest.getLastName());
        cwProfile.setEmailId(caseWorkersProfileUpdationRequest.getEmailId());
        cwProfile.setSuspended(caseWorkersProfileUpdationRequest.getSuspended());
        caseWorkerProfileRepo.save(cwProfile);
        return CaseWorkersProfileUpdationResponse
            .caseWorkersProfileUpdationResponse().userId(cwProfile.getCaseWorkerId()).build();
    }
}
