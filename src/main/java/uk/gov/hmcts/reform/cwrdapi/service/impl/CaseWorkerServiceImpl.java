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

    CaseWorkerProfile caseWorkerProfile = null;


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

                    //caseWorkerProfiles.add(caseWorkerProfile);
                    caseWorkerProfileRepo.save(caseWorkerProfile);
                    log.info("case worker profile inserted::");
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

        //UP Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (responseEntity.getStatusCode().is2xxSuccessful() && null != responseEntity.getBody()) {

            UserProfileCreationResponse userProfileCreationResponse
                    = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
            // case worker profile data
            caseWorkerProfile = new CaseWorkerProfile
                    (userProfileCreationResponse.getIdamId(),cwrdProfileRequest.getFirstName(),
                     cwrdProfileRequest.getLastName(),cwrdProfileRequest.getEmailId(),
                     1l, cwrdProfileRequest.getRegionId(), cwrdProfileRequest.getRegion());

            //Locations data setup to caseworker profile
            cwrdProfileRequest.getBaseLocations().forEach(location -> {

                CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(userProfileCreationResponse.getIdamId(),
                        location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
                cwLocations.add(caseWorkerLocation);
            });
            caseWorkerProfile.setCaseWorkerLocations(cwLocations);

            //caseworker roles assign setup to caseworker profile
            cwrdProfileRequest.getRoles().forEach( role -> {

                roleTypes.stream().forEach(roleType -> {
                    if (role.getRole().equalsIgnoreCase(roleType.getDescription().trim())){
                        CaseWorkerRole workerRole = new CaseWorkerRole(userProfileCreationResponse.getIdamId(),
                                roleType.getRoleId(),role.isPrimaryFlag());
                        caseWorkerRoles.add(workerRole);
                    }
                });

            });

            caseWorkerProfile.setCaseWorkerRoles(caseWorkerRoles);
            //caseworkerworkarea data setup to caseworker profile
            cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
                CaseWorkerWorkArea caseWorkerWorkArea =  new CaseWorkerWorkArea(userProfileCreationResponse.getIdamId(),
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


        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
                new HashSet<String>();
        userRoles.add("cwd-user");
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


    Set<String> getUserRolesByRoleId(CaseWorkersProfileCreationRequest cwrdProfileRequest) {


        cwrdProfileRequest.getRoles().forEach( role -> {

            roleTypes.stream().forEach(roleType -> {
                if (role.getRole().equalsIgnoreCase(roleType.getDescription().trim())){


                }
            });

        });

        return new HashSet<>();
    }
}

