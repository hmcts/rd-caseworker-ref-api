package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    static RoleTypeRepository roleTypeRepository;

    @Autowired
    static UserTypeRepository userTypeRepository;

    @Autowired
    CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    static List<RoleType> roleTypes = new ArrayList<>();

    static List<UserType> userTypes = new ArrayList<>();

    CaseWorkerProfile caseWorkerProfile = null;


    @Override
    public ResponseEntity<Object> saveOrUpdateOrDeleteCaseWorkerUserProfiles(List<CaseWorkersProfileCreationRequest>
                                                                           cwrsProfilesCreationRequest) {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        try {
            getRolesAndUserTypes();
            cwrsProfilesCreationRequest.forEach(cwrProfileCreationRequest -> {
                CaseWorkerProfile caseWorkerProfileRespose = null;

                caseWorkerProfile = caseWorkerProfileRepo
                        .findByEmailId(cwrProfileCreationRequest.getEmailId());
                if (Objects.isNull(caseWorkerProfile)) {

                    caseWorkerProfileRespose = createCaseWorkerProfile(cwrProfileCreationRequest);
                    if (Objects.nonNull(caseWorkerProfileRespose)) {
                        // collecting all the successfully case worker profiles to save in caseworker db.
                        caseWorkerProfiles.add(caseWorkerProfileRespose);

                    }
                } else if (Objects.nonNull(caseWorkerProfile) && !caseWorkerProfile.getDeleteFlag()) {

                    //update the existing case worker profile logic
                } else if (caseWorkerProfile.getDeleteFlag()) {

                    // updating the status in idam to suspend.
                }
            });

            /**  caseworker profile bacth save
             in caseWorkerProfile request contains all the associated entities information and while
             saving it automatically saves data in the sub entities like cwlocation, cwWorkArea,cwRole and
             caseworker profile and no need to explicitly invoke the save method for each entities.
             */
            caseWorkerProfileRepo.saveAll(caseWorkerProfiles);
            log.info("{}::case worker profiles inserted::" + caseWorkerProfiles.size(), loggingComponentName);

        } catch (Exception exp) {

            log.error("{}:: createCaseWorkerUserProfiles failed" + exp, loggingComponentName);
        }
        return ResponseEntity
                .status(201)
                .body(new CaseWorkerProfileCreationResponse("Case Worker Profiles Created."));
    }

    public CaseWorkerProfile createCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();

        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()
                && null != responseEntity.getBody()) {

            UserProfileCreationResponse userProfileCreationResponse
                    = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
            // case worker profile request mapping
            caseWorkerProfile = new CaseWorkerProfile(userProfileCreationResponse.getIdamId(),
                    cwrdProfileRequest.getFirstName(), cwrdProfileRequest.getLastName(),cwrdProfileRequest.getEmailId(),
                            getUserTypeIdByDesc(cwrdProfileRequest.getUserType()), cwrdProfileRequest.getRegionId(),
                            cwrdProfileRequest.getRegion());

            //Locations data request mapping
            cwrdProfileRequest.getBaseLocations().forEach(location -> {

                CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(userProfileCreationResponse.getIdamId(),
                        location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
                cwLocations.add(caseWorkerLocation);
            });

            //Locations data setting to case worker profile
            caseWorkerProfile.setCaseWorkerLocations(cwLocations);

            //caseworker roles request mapping
            cwrdProfileRequest.getRoles().forEach(role -> {

                roleTypes.stream().forEach(roleType -> {
                    if (role.getRole().equalsIgnoreCase(roleType.getDescription().trim())) {
                        CaseWorkerRole workerRole = new CaseWorkerRole(userProfileCreationResponse.getIdamId(),
                                roleType.getRoleId(),role.isPrimaryFlag());
                        caseWorkerRoles.add(workerRole);
                    }
                });

            });
            //caseWorkerRoles data setting to case worker profile
            caseWorkerProfile.setCaseWorkerRoles(caseWorkerRoles);

            //caseworkerworkarea request mapping
            cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
                CaseWorkerWorkArea caseWorkerWorkArea =  new CaseWorkerWorkArea(userProfileCreationResponse.getIdamId(),
                        caseWorkerWorkAreaRequest.getAreaOfWork(), caseWorkerWorkAreaRequest.getServiceCode());
                caseWorkerWorkAreas.add(caseWorkerWorkArea);
            });

            //caseWorkerWorkAreas setting to case worker profile
            caseWorkerProfile.setCaseWorkerWorkAreas(caseWorkerWorkAreas);
        } else {
            //Add failed request to list and save to the exception table
            log.error("{}::Idam register user failed", loggingComponentName);

        }
        return caseWorkerProfile;
    }


    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Response response = null;
        Object clazz = null;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(cwrdProfileRequest));
            if (response.status() == 201 || response.status() == 409) {
                clazz = UserProfileCreationResponse.class;
            } else {
                clazz = ErrorResponse.class;
            }

        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {}", loggingComponentName, ex.status());
            clazz = ErrorResponse.class;
        }
        return JsonFeignResponseUtil.toResponseEntity(response, clazz);
    }

    /**
     * creating user profile request.
     * @return
     * */
    public UserProfileCreationRequest createUserProfileRequest(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
                new HashSet<String>();
        userRoles.add("cwd-user");

        Set<String> idamRoles = getUserRolesByRoleId(cwrdProfileRequest);
        if (idamRoles.size() > 0) {
            userRoles.addAll(idamRoles);
        }
        //Creating user profile request
        return  new UserProfileCreationRequest(
                cwrdProfileRequest.getEmailId(),
                cwrdProfileRequest.getFirstName(),
                cwrdProfileRequest.getLastName(),
                LanguagePreference.EN,
                UserCategory.CASEWORKER,
                UserTypeRequest.INTERNAL,
                userRoles,
                false);
    }


    public static void getRolesAndUserTypes() {

        if (roleTypes.isEmpty()) {
            roleTypes = roleTypeRepository.findAll();
        }

        if (userTypes.isEmpty()) {
            userTypes = userTypeRepository.findAll();
        }
    }

    Set<String> getUserRolesByRoleId(CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerIdamRoleAssociation>  idamRolesInRequest = new ArrayList<>();
        cwrdProfileRequest.getRoles().forEach(role -> {
            roleTypes.forEach(roleType -> {
                if (role.getRole().equalsIgnoreCase(roleType.getDescription().trim())) {
                    List<CaseWorkerIdamRoleAssociation>  caseWorkerIdamRoles = cwIdamRoleAssocRepository
                            .findByRoleType(roleType);
                    idamRolesInRequest.addAll(caseWorkerIdamRoles);
                }
            });

        });

        Set<String> idamRoles = idamRolesInRequest.stream().map(idamRole -> {
            return idamRole.getIdamRole();
        }).collect(Collectors.toSet());

        return idamRoles;
    }

    public Long getUserTypeIdByDesc(String  userTypeReq) {
        Long userTypeId = 0L;
        for (UserType userType : userTypes) {
            if (userType.getDescription().equalsIgnoreCase(userTypeReq.trim())) {
                userTypeId = userType.getUserTypeId();
            }
        }
        return userTypeId;
    }
}

