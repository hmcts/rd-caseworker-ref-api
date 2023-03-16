package uk.gov.hmcts.reform.cwrdapi.controllers.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.client.domain.StaffProfileWithServiceName;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.RequestUtils;

import javax.validation.constraints.NotEmpty;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_DATA_FOUND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUIRED_PARAMETER_CCD_SERVICE_NAMES_IS_EMPTY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;

@RequestMapping(
        path = "/refdata/internal/staff"
)
@RestController
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class StaffReferenceInternalController {


    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${refresh.pageSize}")
    private int configPageSize;

    @Value("${refresh.sortColumn}")
    private String configSortColumn;

    @Autowired
    CaseWorkerService caseWorkerService;

    @Operation(
            summary = "This API returns the Staff(Case Worker) "
                    + "profiles based on Service Name and Pagination parameters",
            description = "**IDAM Role to access API** :\n cwd-system-user",
            security = {
                    @SecurityRequirement(name = "ServiceAuthorization"),
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The Staff profiles have been retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StaffProfileWithServiceName.class))
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
                    responseCode = "404",
                    description = NO_DATA_FOUND,
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = INTERNAL_SERVER_ERROR,
                    content = @Content
            )
    })
    @GetMapping(
            path = "/usersByServiceName",
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
        log.info("{}:: Fetching the staff details to refresh role assignment for ccd service names {}",
                loggingComponentName, ccdServiceNames);
        if (StringUtils.isBlank(ccdServiceNames)) {
            throw new InvalidRequestException(REQUIRED_PARAMETER_CCD_SERVICE_NAMES_IS_EMPTY);
        }

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(pageNumber, pageSize,
                sortColumn, sortDirection, configPageSize, configSortColumn, CaseWorkerProfile.class);


        return caseWorkerService.fetchStaffProfilesForRoleRefresh(ccdServiceNames, pageRequest);
    }
}
