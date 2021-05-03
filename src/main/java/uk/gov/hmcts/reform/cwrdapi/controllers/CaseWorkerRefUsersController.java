package uk.gov.hmcts.reform.cwrdapi.controllers;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PaginatedStaffProfile;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.RequestUtils;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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

    @Autowired
    CaseWorkerService caseWorkerService;

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
        long time1 = System.currentTimeMillis();
        List<CaseWorkerProfile> processedCwProfiles =
                caseWorkerService.processCaseWorkerProfiles(caseWorkersProfileCreationRequest);
        log.info("{}:: Time taken to process the given file is {}", loggingComponentName,
                (System.currentTimeMillis() - time1));
        if (isNotEmpty(processedCwProfiles)) {
            long time2 = System.currentTimeMillis();
            caseWorkerService.publishCaseWorkerDataToTopic(processedCwProfiles);
            log.info("{}:: Time taken to publish the message is {}", loggingComponentName,
                    (System.currentTimeMillis() - time2));
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
        long startTime = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(userRequest.getUserIds())) {
            throw new InvalidRequestException("Caseworker request is empty");
        }
        ResponseEntity<Object> responseEntity =
                caseWorkerService.fetchCaseworkersById(userRequest.getUserIds());
        log.info("{}::Time taken to fetch {} users is {}",loggingComponentName,
                userRequest.getUserIds().size(),
                (Math.subtractExact(System.currentTimeMillis(), startTime)));
        return responseEntity;

    }

    @ApiOperation(
            value = "This API returns the Staff(Case Worker) profiles based on Service Name and Pagination parameters",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "The caseworker profiles have been retrieved successfully",
                    response = PaginatedStaffProfile.class
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
    @GetMapping(
            path = "/get-users-by-service-name",
            produces = APPLICATION_JSON_VALUE
    )
    @Secured("cwd-system-user")
    public ResponseEntity<Object> fetchStaffByCcdServiceNames(
            @RequestParam(name = "ccd_service_names") @NotEmpty String ccdServiceNames,
            @RequestParam(name = "page_size", required = false) Integer pageSize,
            @RequestParam(name = "page_number", required = false) Integer pageNumber,
            @RequestParam(name = "sort_direction", required = false) String sortDirection,
            @RequestParam(name = "sort_column", required = false) String sortColumn
    ) {
        if (StringUtils.isBlank(ccdServiceNames)) {
            throw new InvalidRequestException("The required parameter 'ccd_service_names' is empty");
        }
        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(pageNumber, pageSize,
                sortColumn, sortDirection, loggingComponentName, configPageSize, configSortColumn);
        return caseWorkerService.fetchStaffProfilesForRoleRefresh(ccdServiceNames, pageRequest);
    }
}
