package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.CwrdApiException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
@Setter
public class CaseWorkerServiceImpl implements CaseWorkerService {


    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    private static final String IDAM_ERROR_MESSAGE = "{}:: Idam register user failed with status code : %s";

    static List<RoleType> roleTypes = new ArrayList<>();

    @Override
    public int createCaseWorkerUserProfiles(List<CaseWorkersProfileCreationRequest> caseWorkersProfilesCreationRequest)
    {

        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        if (roleTypes.size() == 0) {

            roleTypes = roleTypeRepository.findAll();
        }

        caseWorkersProfilesCreationRequest.forEach( caseWorkerProfileCreationRequest -> {
            CaseWorkerProfile caseWorkerProfile = null;
            if (Objects.isNull(caseWorkerProfileRepo.findByEmailId(caseWorkerProfileCreationRequest.getEmailId()))) {

                caseWorkerProfile = saveCaseWorkerProfile(caseWorkerProfileCreationRequest);
                if (Objects.nonNull(caseWorkerProfile)) {

                    caseWorkerProfiles.add(caseWorkerProfile);
                    caseWorkerProfileRepo.save(caseWorkerProfile);
                }


            } else {

                //update the existing case worker profile logic
            }
        });


        return 0;
    }

    public CaseWorkerProfile saveCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest ) {

        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = null;
        //UP Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (responseEntity.getStatusCode().is2xxSuccessful() && null != responseEntity.getBody()) {
            UserProfileCreationResponse userProfileCreationResponse
                    = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());

            caseWorkerProfile = new CaseWorkerProfile
                    (userProfileCreationResponse.getIdamId(),cwrdProfileRequest.getFirstName(),
                     cwrdProfileRequest.getLastName(),cwrdProfileRequest.getEmailId(),
                     1l, cwrdProfileRequest.getRegion());

            //Locations data
            cwrdProfileRequest.getBaseLocations().forEach(location -> {

                CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(location.getLocationId(),
                        location.getLocation(), location.isPrimaryFlag());
                cwLocations.add(caseWorkerLocation);
            });
            caseWorkerProfile.setCaseWorkerLocations(cwLocations);

            //caseworker roles assign
            cwrdProfileRequest.getRoles().forEach( role -> {

                roleTypes.stream().forEach(roleType -> {
                    if (role.getRole().equalsIgnoreCase(roleType.getDescription().trim())){
                        CaseWorkerRole workerRole = new CaseWorkerRole(roleType.getRoleId(),role.isPrimaryFlag());
                        caseWorkerRoles.add(workerRole);
                    }
                });

            });

            caseWorkerProfile.setCaseWorkerRoles(caseWorkerRoles);

            cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
                CaseWorkerWorkArea caseWorkerWorkArea =  new CaseWorkerWorkArea(
                        caseWorkerWorkAreaRequest.getAreaOfWork(), caseWorkerWorkAreaRequest.getServiceCode());
                caseWorkerWorkAreas.add(caseWorkerWorkArea);
            });
            caseWorkerProfile.setCaseWorkerWorkAreas(caseWorkerWorkAreas);


        } else {
            log.error("{}:: " + String.format(IDAM_ERROR_MESSAGE, responseEntity.getStatusCode().value()),
                    loggingComponentName);

        }
        return caseWorkerProfile;
    }

    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = new HashSet<String>(cwrdProfileRequest.getIdamRoles());
        userRoles.add("CWD_USER");
        //Creating user profile request
        UserProfileCreationRequest userCreationRequest = new UserProfileCreationRequest(
                cwrdProfileRequest.getEmailId(),
                cwrdProfileRequest.getFirstName(),
                cwrdProfileRequest.getLastName(),
                LanguagePreference.EN,
                UserCategory.CASEWORKER,
                UserType.INTERNAL,
                userRoles,
                false);

        try (Response response = userProfileFeignClient.createUserProfile(userCreationRequest)) {
            Object clazz = response.status() > 300 ? ErrorResponse.class : UserProfileCreationResponse.class;
            return JsonFeignResponseUtil.toResponseEntity(response, clazz);
        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {}", loggingComponentName, ex.status());
            throw new CwrdApiException(HttpStatus.valueOf(ex.status()), "UserProfile api failed!!");
        }
    }
}

