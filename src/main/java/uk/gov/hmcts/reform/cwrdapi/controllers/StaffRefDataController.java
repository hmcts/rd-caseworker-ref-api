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

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.service.StaffProfileService;

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
            value = "This API creates staff user profile",
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
    @PutMapping (
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            path = {"/profile"}
    )
    @Secured("staff-admin")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<StaffProfileCreationResponse>
    updateStaffUserProfile(@RequestBody StaffProfileCreationRequest
                                   staffProfileCreationRequest) {
        log.debug("Inside updateStaffUserProfile Controller");
        StaffProfileCreationResponse response = null;

        response = staffProfileService.processStaffProfileUpdate(staffProfileCreationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
