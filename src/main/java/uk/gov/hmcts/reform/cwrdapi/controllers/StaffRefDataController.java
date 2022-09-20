package uk.gov.hmcts.reform.cwrdapi.controllers;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FORBIDDEN_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UNAUTHORIZED_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.removeEmptySpaces;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPagination;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateSearchString;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

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
    StaffRefDataService caseWorkerService;

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
//    @Secured("staff-admin")
    public ResponseEntity<Object> searchStaffUserByName(
        @RequestHeader(name = "page-number", required = false) Integer pageNumber,
        @RequestHeader(name = "page-size", required = false) Integer pageSize,
        @RequestParam(value = "search") @NotEmpty @NotNull String searchString) {

        validateSearchString(removeEmptySpaces(searchString));
        PageRequest pageRequest = validateAndBuildPagination(pageSize, pageNumber, configPageSize, configPageNumber);

        List<SearchStaffUserResponse> searchResponse =
            caseWorkerService.retrieveStaffUserByName(searchString, pageRequest);
        int totalRecords = searchResponse.size();

        //if no data found return 200 with empty response
        return ResponseEntity
            .status(200)
            .header("total-records",String.valueOf(totalRecords))
            .body(searchResponse);
    }

}
