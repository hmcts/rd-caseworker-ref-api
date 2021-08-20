package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfilesDeletionResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerDeleteService;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
@Setter
public class CaseWorkerDeleteServiceImpl implements CaseWorkerDeleteService {

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Transactional
    public CaseWorkerProfilesDeletionResponse deleteByUserId(String userId) {
        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();

        Response userProfileResponse = userProfileFeignClient.deleteUserProfile(userId, null);

        if (userProfileResponse.status() == NO_CONTENT.value() || userProfileResponse.status() == NOT_FOUND.value()) {
            Optional<CaseWorkerProfile> caseWorkerProfile = caseWorkerProfileRepo.findByCaseWorkerId(userId.trim());

            return validateUserAfterUpDelete(caseWorkerProfile, userId, userProfileResponse.status());

        } else {
            deletionResponse.setMessage("UP Delete request failed for userId: " + userId
                    + ". With the following UP message: " + userProfileResponse.reason());
            deletionResponse.setStatusCode(userProfileResponse.status());
            return deletionResponse;
        }
    }

    @Transactional
    public CaseWorkerProfilesDeletionResponse deleteByEmailPattern(String emailPattern) {
        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();

        List<CaseWorkerProfile> userProfiles = caseWorkerProfileRepo.findByEmailIdIgnoreCaseContaining(emailPattern);

        if (userProfiles.isEmpty()) {
            throw new ResourceNotFoundException("No User Profiles found for email pattern: " + emailPattern);
        }

        Response userProfileResponse = userProfileFeignClient.deleteUserProfile(null, emailPattern);

        if (userProfileResponse.status() == NO_CONTENT.value() || userProfileResponse.status() == NOT_FOUND.value()) {
            return deleteUserProfiles(userProfiles);

        } else {
            deletionResponse.setMessage("UP Delete request failed for emailPattern: " + emailPattern);
            deletionResponse.setStatusCode(userProfileResponse.status());
            return deletionResponse;
        }
    }

    public CaseWorkerProfilesDeletionResponse validateUserAfterUpDelete(
            Optional<CaseWorkerProfile> caseWorkerProfile, String userId, int status) {
        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();

        if (caseWorkerProfile.isPresent()) {
            return deleteUserProfiles(singletonList(caseWorkerProfile.get()));

        } else if (status == NO_CONTENT.value()) {
            deletionResponse.setMessage("User deleted in UP but was not present in CRD with userId: " + userId);

        } else {
            deletionResponse.setMessage("User was not present in UP or CRD with userId: " + userId);
        }

        deletionResponse.setStatusCode(status);
        return deletionResponse;
    }

    public CaseWorkerProfilesDeletionResponse deleteUserProfiles(List<CaseWorkerProfile> userProfiles) {
        caseWorkerProfileRepo.deleteAll(userProfiles);

        return new CaseWorkerProfilesDeletionResponse(NO_CONTENT.value(),
                "Case Worker Profiles successfully deleted");
    }
}
