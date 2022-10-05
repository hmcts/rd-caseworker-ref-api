package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffProfileService;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;

@RequestMapping(
        path = "/refdata/case-worker"
)
@RestController
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class StaffRefDataController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    StaffRefDataService staffRefDataService;

    @Autowired
    StaffProfileService staffProfileService;

    @ApiOperation(
            value = "This API gets the user types from staff reference data",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully fetched the user types",
                    response = StaffRefDataUserTypesResponse.class
            ),
            @ApiResponse(
                    code = 401,
                    message = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    code = 500,
                    message = INTERNAL_SERVER_ERROR
            )
    })
    @GetMapping(
            path = "/user-type",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("staff-admin")
    public ResponseEntity<Object> fetchUserTypes() {
        log.info("{} : Fetching the user types", loggingComponentName);
        StaffRefDataUserTypesResponse.StaffRefDataUserTypesResponseBuilder staffReferenceDataUserTypesResponseBuilder
                = StaffRefDataUserTypesResponse.builder();
        List<UserType> userTypes = staffRefDataService.fetchUserTypes();
        List<StaffRefDataUserType> refDataUserTypes = userTypes.stream()
                .map(StaffRefDataUserType::new)
                .toList();
        staffReferenceDataUserTypesResponseBuilder.userTypes(refDataUserTypes);
        log.debug("refDataUserTypes = {}", refDataUserTypes);
        return ResponseEntity
                .status(200)
                .body(staffReferenceDataUserTypesResponseBuilder.build());
    }

    @ApiOperation(
            value = "This API is used to retrieve the Job Title's ",
            notes = "This API will be invoked by user having idam role of staff-admin",

            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully retrieved list of Job Titles for the request provided",
                    response = StaffRefJobTitleResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @GetMapping(
            produces = APPLICATION_JSON_VALUE,
            path = {"/job-title"}
    )
    @Secured("staff-admin")
    public ResponseEntity<Object> retrieveJobTitles() {

        log.info("{} : Fetching the Job Titles", loggingComponentName);
        StaffRefJobTitleResponse.StaffRefJobTitleResponseBuilder staffRefJobTitleResponseBuilder
                = StaffRefJobTitleResponse.builder();
        List<RoleType> roleType = staffRefDataService.getJobTitles();
        List<StaffRefDataJobTitle> refDataJobTitles = roleType.stream()
                .map(StaffRefDataJobTitle::new)
                .toList();
        staffRefJobTitleResponseBuilder.jobTitles(refDataJobTitles);
        log.debug("refDataJobTitles = {}", refDataJobTitles);
        return ResponseEntity
                .status(200)
                .body(staffRefJobTitleResponseBuilder.build());

    }

    @ApiOperation(
            value = "This API creates staff user profile",
            notes = "This API will be invoked by user having idam role of staff-admin",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "Successfully created staff user profile",
                    response = StaffProfileCreationResponse.class,
                    responseContainer = "list"
            ),
            @ApiResponse(
                    code = 400,
                    message = BAD_REQUEST
            ),
            @ApiResponse(
                    code = 401,
                    message = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    code = 500,
                    message = INTERNAL_SERVER_ERROR
            )
    })
    @PostMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            path = {"/profile"}
    )

    //@Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<StaffProfileCreationResponse> createStaffUserProfile(@RequestBody StaffProfileCreationRequest
                                                                                       staffProfileCreationRequest) {

        log.info("Inside createStaffUserProfile Controller");
        StaffProfileCreationResponse response = null;

        response = staffProfileService.processStaffProfileCreation(staffProfileCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @ApiOperation(
            value = "This API update staff user profile",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "Successfully updated staff user profile",
                    response = StaffProfileCreationResponse.class,
                    responseContainer = "list"
            ),
            @ApiResponse(
                    code = 400,
                    message = BAD_REQUEST
            ),
            @ApiResponse(
                    code = 401,
                    message = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    code = 403,
                    message = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    code = 500,
                    message = INTERNAL_SERVER_ERROR
            )
    })
    @PutMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            path = {"/profile"}
    )
    //@Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<StaffProfileCreationResponse> updateStaffUserProfile(@RequestBody StaffProfileCreationRequest
                                                                                       staffProfileCreationRequest) {
        log.info("Inside updateStaffUserProfile Controller");
        StaffProfileCreationResponse response = null;

        response = staffProfileService.updateStaffProfile(staffProfileCreationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            path = {"/profile/test"}
    )
    //@Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<StaffProfileCreationResponse> updateStaffUserProfileTest(@RequestBody StaffProfileCreationRequest
                                                                                       staffProfileCreationRequest) {
        log.info("Inside updateStaffUserProfile Controller");

        List<StaffProfileCreationRequest> cwUiRequests = Collections.singletonList(staffProfileCreationRequest);
        //response = staffProfileService.updateStaffProfile(staffProfileCreationRequest);
        List<CaseWorkerProfile> caseWorkerProfiles = staffProfileService.updateStaffProfiles(cwUiRequests);
        StaffProfileCreationResponse response = new StaffProfileCreationResponse();
        CaseWorkerProfile caseWorkerProfile = caseWorkerProfiles.get(0);

        response.setCaseWorkerId(caseWorkerProfile.getCaseWorkerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
