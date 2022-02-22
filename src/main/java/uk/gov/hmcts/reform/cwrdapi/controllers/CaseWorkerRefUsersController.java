package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfilesDeletionResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerDeleteService;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.NO_USER_ID_OR_EMAIL_PATTERN_PROVIDED_TO_DELETE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.API_IS_NOT_AVAILABLE_IN_PROD_ENV;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_DATA_FOUND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.trimIdamRoles;

@RequestMapping(
        path = "/refdata/case-worker/users"
)
@RestController
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class CaseWorkerRefUsersController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${refresh.pageSize}")
    private int configPageSize;

    @Value("${refresh.sortColumn}")
    private String configSortColumn;

    @Value("${environment_name}")
    private String environmentName;

    @Autowired
    CaseWorkerService caseWorkerService;

    @Autowired
    CaseWorkerDeleteService caseWorkerDeleteService;

    @ApiOperation(
            hidden = true,
            value = "This API creates caseworker profiles",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "Successfully created caseworker user profiles",
                    response = String.class,
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
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("cwd-admin")
    @Transactional
    public ResponseEntity<Object> createCaseWorkerProfiles(@RequestBody List<CaseWorkersProfileCreationRequest>
                                                                   caseWorkersProfileCreationRequest) {
        if (CollectionUtils.isEmpty(caseWorkersProfileCreationRequest)) {

            throw new InvalidRequestException("Caseworker Profiles Request is empty");
        }

        CaseWorkerProfileCreationResponse.CaseWorkerProfileCreationResponseBuilder caseWorkerProfileCreationResponse =
                CaseWorkerProfileCreationResponse
                        .builder();
        trimIdamRoles(caseWorkersProfileCreationRequest);

        List<CaseWorkerProfile> processedCwProfiles =
                caseWorkerService.processCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        if (isNotEmpty(processedCwProfiles)) {

            caseWorkerService.publishCaseWorkerDataToTopic(processedCwProfiles);

            List<String> caseWorkerIds = processedCwProfiles.stream()
                    .map(CaseWorkerProfile::getCaseWorkerId)
                    .collect(Collectors.toUnmodifiableList());
            caseWorkerProfileCreationResponse
                    .caseWorkerRegistrationResponse(REQUEST_COMPLETED_SUCCESSFULLY)
                    .messageDetails(format(RECORDS_UPLOADED, caseWorkerIds.size()))
                    .caseWorkerIds(caseWorkerIds);
        }
        return ResponseEntity
                .status(201)
                .body(caseWorkerProfileCreationResponse.build());
    }


    @ApiOperation(
            value = "This API gets the User details from Caseworker Profile",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully fetched the Caseworker profile(s)",
                    response = CaseWorkerProfile.class
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
                    code = 404,
                    message = NO_DATA_FOUND
            ),
            @ApiResponse(
                    code = 500,
                    message = INTERNAL_SERVER_ERROR
            )
    })
    @PostMapping(
            path = "/fetchUsersById",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    @Secured({"cwd-system-user"})
    public ResponseEntity<Object> fetchCaseworkersById(@RequestBody UserRequest userRequest) {
        log.info("Fetching the details of {} users", userRequest.getUserIds().size());

        if (CollectionUtils.isEmpty(userRequest.getUserIds())) {
            throw new InvalidRequestException("Caseworker request is empty");
        }

        return caseWorkerService.fetchCaseworkersById(userRequest.getUserIds());

    }

    @ApiOperation(value = "Delete Case Worker Profiles by User ID or Email Pattern",
            notes = "This API is only for use in non Prod environments",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            })
    @ApiResponses({
            @ApiResponse(
                    code = 204,
                    message = "Case Worker Profiles deleted successfully",
                    response = CaseWorkerProfilesDeletionResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "An invalid request has been provided"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized Error : The requested resource is restricted and requires authentication"
            ),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })

    @DeleteMapping(
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public ResponseEntity<CaseWorkerProfilesDeletionResponse> deleteCaseWorkerProfileByIdOrEmailPattern(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "emailPattern", required = false) String emailPattern) {

        /**
         * This API will need to be revisited if it is to be used for business functionality.
         */

        log.info("ENVIRONMENT NAME:::::: " + environmentName);

        if ("PROD".equalsIgnoreCase(environmentName)) {
            throw new AccessDeniedException(API_IS_NOT_AVAILABLE_IN_PROD_ENV);
        }

        CaseWorkerProfilesDeletionResponse resource;

        if (isNotBlank(userId)) {
            resource = caseWorkerDeleteService.deleteByUserId(userId);

        } else if (isNotBlank(emailPattern)) {
            resource = caseWorkerDeleteService.deleteByEmailPattern(emailPattern);

        } else {
            throw new InvalidRequestException(NO_USER_ID_OR_EMAIL_PATTERN_PROVIDED_TO_DELETE.getErrorMessage());
        }

        return ResponseEntity.status(resource.getStatusCode()).body(resource);
    }
}
