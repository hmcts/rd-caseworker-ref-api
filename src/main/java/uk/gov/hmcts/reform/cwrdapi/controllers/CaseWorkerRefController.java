package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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


    @Operation(
            summary = "This API allows uploading of excel files that contain caseworker information "
                    + "and mappings between caseworker and IDAM roles",
            description = "This API will be invoked by user having idam role of cwd-admin",
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
    @PostMapping(value = "/upload-file",
            consumes = "multipart/form-data")
    @Secured("cwd-admin")
    public ResponseEntity<Object> caseWorkerFileUpload(@RequestParam(FILE) MultipartFile file) {
        return caseWorkerServiceFacade.processFile(file);
    }

    @Operation(
            hidden = true,
            summary = "This API builds the idam role mappings for case worker roles",
            description = "This API will be invoked by user having idam role of cwd-admin",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully built idam role mappings for case worker roles",
                    content = @Content(schema = @Schema(implementation = IdamRolesMappingResponse.class))
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
