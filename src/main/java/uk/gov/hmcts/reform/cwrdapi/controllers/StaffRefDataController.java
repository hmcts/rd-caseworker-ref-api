package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
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
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
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
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateSearchRequest;
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

    @Operation(
            summary = "This API allows the search of staff user by their name or surname.",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = REQUEST_COMPLETED_SUCCESSFULLY,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = BAD_REQUEST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR
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


    @Operation(

            summary = "This API is used to retrieve the service specific skills ",
            description = "This API will be invoked by user having idam role of staff-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of ServiceSkills for the request provided",
                    content = @Content(schema = @Schema(implementation = StaffWorkerSkillResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR
            )
    })
    @GetMapping(
            produces = APPLICATION_JSON_VALUE,
            path = {"/skill"}
    )
    @Secured("staff-admin")
    public ResponseEntity<StaffWorkerSkillResponse> retrieveAllServiceSkills(
            @RequestParam(value = "service_codes", required = false)  String serviceCodes
    ) {
        log.info("StaffRefDataController.retrieveAllServiceSkills Calling Service layer");

        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataService.getServiceSkills(serviceCodes);

        return ResponseEntity.ok().body(staffWorkerSkillResponse);
    }

    @Operation(
            description = "This API gets the user types from staff reference data",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully fetched the user types",
                    content = @Content(schema = @Schema(implementation = StaffRefDataUserTypesResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR,
                    content = @Content
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


    @Operation(
            summary = "This API is used to retrieve the Job Title's ",
            description = "This API will be invoked by user having idam role of staff-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of Job Titles for the request provided",
                    content = @Content(schema = @Schema(implementation = StaffRefJobTitleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Forbidden Error: Access denied",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content
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

    @Operation(
            summary = "This API creates staff user profile",
            description = "This API will be invoked by user having idam role with cwd-admin and staff-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully created staff user profile",
                    content = @Content(array =
                            @ArraySchema(schema = @Schema(implementation = StaffProfileCreationResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = BAD_REQUEST,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR,
                    content = @Content
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

    @Operation(
            summary = "This API allows the Advance search of staff",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = REQUEST_COMPLETED_SUCCESSFULLY
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = BAD_REQUEST
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR
            )
    })
    @Validated
    @GetMapping(path = "/profile/search",
            produces = APPLICATION_JSON_VALUE)
    @Secured("staff-admin")
    public ResponseEntity<List<SearchStaffUserResponse>> searchStaffProfile(
            @RequestHeader(name = "page-number", required = false) Integer pageNumber,
            @RequestHeader(name = "page-size", required = false) Integer pageSize,
            @ParameterObject SearchRequest searchRequest) {
        validateSearchRequest(searchRequest);
        var pageRequest = validateAndBuildPagination(pageSize, pageNumber, configPageSize,
                configPageNumber);
        return staffRefDataService.retrieveStaffProfile(searchRequest, pageRequest);
    }

    @Operation(
            summary = "This API updates staff user profile",
            description = "This API will be invoked by user having idam role with staff-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated staff user profile",
                    content = @Content(array =
                            @ArraySchema(schema = @Schema(implementation = StaffProfileCreationResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = BAD_REQUEST,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = UNAUTHORIZED_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = FORBIDDEN_ERROR,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR,
                    content = @Content
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
        StaffProfileCreationResponse staffProfileCreationResponse;

        if (staffProfileCreationRequest.isResendInvite()) {
            staffProfileCreationResponse = staffRefDataService.reinviteStaffProfile(staffProfileCreationRequest);
        } else {
            staffProfileCreationResponse = staffRefDataService.updateStaffProfile(staffProfileCreationRequest);
        }
        if (isNotEmpty(staffProfileCreationResponse)) {

            staffRefDataService.publishStaffProfileToTopic(staffProfileCreationResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(staffProfileCreationResponse);
    }
}
