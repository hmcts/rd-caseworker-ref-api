package uk.gov.hmcts.reform.cwrdapi.controllers;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping(
        path = "/refdata/location/orgServices"
)
@RestController
@Slf4j
public class CaseWorkerRefController {

    @Autowired
    CaseWorkerService caseWorkerService;

    @ApiOperation(
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
                    message = "Bad Request"
            ),
            @ApiResponse(
                    code = 404,
                    message = "No Caseworker profiles found with the given ID"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
            )
    })
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createCaseWorkerProfiles(@RequestBody List<CaseWorkersProfileCreationRequest>
                                                                   caseWorkersProfileCreationRequest) {
        if (CollectionUtils.isEmpty(caseWorkersProfileCreationRequest)) {

            throw new InvalidRequestException("Caseworker Profiles Request is empty");
        }

        return caseWorkerService.createCaseWorkerUserProfiles(caseWorkersProfileCreationRequest);
    }
}
