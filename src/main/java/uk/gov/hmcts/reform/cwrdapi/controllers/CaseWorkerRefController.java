package uk.gov.hmcts.reform.cwrdapi.controllers;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;

@RequestMapping(
        path = "/refdata/case-worker"
)
@RestController
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerRefController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    CaseWorkerService caseWorkerService;

    @Autowired
    CaseWorkerServiceFacade caseWorkerServiceFacade;


    @ApiOperation(
            value = "This API allows uploading of excel files that contain caseworker information "
                    + "and mappings between caseworker and IDAM roles",
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
    @PostMapping(value = "/upload-file",
            consumes = "multipart/form-data")
    @Secured("cwd-admin")
    public ResponseEntity<Object> caseWorkerFileUpload(@RequestParam(FILE)  MultipartFile file) {
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(file);
        return responseEntity;
    }

    @ApiOperation(
            hidden = true,
            value = "This API builds the idam role mappings for case worker roles",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "Successfully built idam role mappings for case worker roles",
                    response = IdamRolesMappingResponse.class
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
            path = "/idam-roles-mapping",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    //Sonar reported the below code as Security Hotspot with LOW priority.
    //Currently it has been made false positive and reviewed as safe.
    @Secured("cwd-admin")
    public ResponseEntity<Object> buildIdamRoleMappings(@RequestBody List<ServiceRoleMapping>
                                                                serviceRoleMappings) {
        if (CollectionUtils.isEmpty(serviceRoleMappings)) {
            throw new InvalidRequestException("ServiceRoleMapping Request is empty");
        }
        IdamRolesMappingResponse idamRolesMappingResponse =
                caseWorkerService.buildIdamRoleMappings(serviceRoleMappings);
        return ResponseEntity
                .status(idamRolesMappingResponse.getStatusCode())
                .body(idamRolesMappingResponse);
    }
}
