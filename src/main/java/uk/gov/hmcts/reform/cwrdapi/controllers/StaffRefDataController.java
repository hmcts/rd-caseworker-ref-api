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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.removeEmptySpaces;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPagination;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateSearchString;



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


    @Value("${search.pageSize}")
    private int configPageSize;

    @Value("${search.pageNumber}")
    private int configPageNumber;



    @Autowired
    StaffRefDataService staffRefDataService;

    @ApiOperation(

        value = "This API allows the search of staff user by their name or surname.",
        authorizations = {
            @Authorization(value = "ServiceAuthorization"),
            @Authorization(value = "Authorization")
        }
    )
    @ApiResponses({
        @ApiResponse(
            code = 201,
            message = REQUEST_COMPLETED_SUCCESSFULLY
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
    @Validated
    @GetMapping(path = "/profile/search-by-name",
                produces = APPLICATION_JSON_VALUE)
    @Secured("staff-admin")
    public ResponseEntity<List<SearchStaffUserResponse>> searchStaffUserByName(
        @RequestHeader(name = "page-number", required = false) Integer pageNumber,
        @RequestHeader(name = "page-size", required = false) Integer pageSize,
        @RequestParam(value = "search") @NotEmpty @NotNull String searchString) {

        validateSearchString(removeEmptySpaces(searchString));
        var pageRequest = validateAndBuildPagination(pageSize, pageNumber, configPageSize, configPageNumber);

        return staffRefDataService.retrieveStaffUserByName(searchString, pageRequest);

    }


    @ApiOperation(

            value = "This API is used to retrieve the service specific skills ",
            notes = "This API will be invoked by user having idam role of staff-admin",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )


    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully retrieved list of ServiceSkills for the request provided",
                    response = StaffWorkerSkillResponse.class
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
            produces = APPLICATION_JSON_VALUE,
            path = {"/skill"}
    )
    @Secured("staff-admin")
    public ResponseEntity<StaffWorkerSkillResponse> retrieveAllServiceSkills() {
        log.info("StaffRefDataController.retrieveAllServiceSkills Calling Service layer");

        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataService.getServiceSkills();

        return ResponseEntity.ok().body(staffWorkerSkillResponse);
    }


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
            path = {"/job-title"},
            produces = APPLICATION_JSON_VALUE
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
            notes = "This API will be invoked by user having idam role with cwd-admin and staff-admin",
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
    @Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    @Transactional
    public ResponseEntity<StaffProfileCreationResponse> createStaffUserProfile(@RequestBody StaffProfileCreationRequest
                                                                                       staffProfileCreationRequest) {

        log.info("Inside createStaffUserProfile Controller");
        StaffProfileCreationResponse staffProfileCreationResponse = null;

        staffProfileCreationResponse = staffRefDataService.processStaffProfileCreation(staffProfileCreationRequest);
        if (isNotEmpty(staffProfileCreationResponse)) {

            staffRefDataService.publishStaffProfileToTopic(staffProfileCreationResponse);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(staffProfileCreationResponse);
    }


    @ApiOperation(
            value = "This API updates staff user profile",
            notes = "This API will be invoked by user having idam role with staff-admin",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
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
    @Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @Transactional
    public ResponseEntity<StaffProfileCreationResponse> updateStaffUserProfile(@RequestBody StaffProfileCreationRequest
                                                                                       staffProfileCreationRequest) {
        log.info("Inside updateStaffUserProfile Controller");
        StaffProfileCreationResponse staffProfileCreationResponse = null;

        staffProfileCreationResponse = staffRefDataService.updateStaffProfile(staffProfileCreationRequest);
        if (isNotEmpty(staffProfileCreationResponse)) {

            staffRefDataService.publishStaffProfileToTopic(staffProfileCreationResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(staffProfileCreationResponse);
    }


}
