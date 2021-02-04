package uk.gov.hmcts.reform.cwrdapi.controllers;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.List;
import java.util.stream.Collectors;

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

@RequestMapping(
        path = "/refdata/case-worker/users"
)
@RestController
@Slf4j
@AllArgsConstructor
public class CaseWorkerRefUsersController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

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
    public ResponseEntity<Object> createCaseWorkerProfiles(@RequestBody List<CaseWorkersProfileCreationRequest>
                                                                   caseWorkersProfileCreationRequest) {
        if (CollectionUtils.isEmpty(caseWorkersProfileCreationRequest)) {

            throw new InvalidRequestException("Caseworker Profiles Request is empty");
        }

        CaseWorkerProfileCreationResponse.CaseWorkerProfileCreationResponseBuilder caseWorkerProfileCreationResponse =
                CaseWorkerProfileCreationResponse
                        .builder()
                        .caseWorkerRegistrationResponse(REQUEST_COMPLETED_SUCCESSFULLY);
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

        if (CollectionUtils.isEmpty(userRequest.getUserIds())) {
            throw new InvalidRequestException("Caseworker request is empty");
        }
        return caseWorkerService.fetchCaseworkersById(userRequest.getUserIds());
    }

}
