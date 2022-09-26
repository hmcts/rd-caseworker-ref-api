package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataJobTitle;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    @ApiOperation(
            value = "This API is used to retrieve the Job Title's ",
            notes = "This API will be invoked by Job Title having idam role of staff-admin",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Successfully retrieved list of Job Titles for the request provided",
                    response = StaffRefJobTitleResponse.class
            ),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request"
            ),
            @ApiResponse(
                    code = 401,
                    message = "Forbidden Error: Access denied"
            ),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error"
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

        if (ObjectUtils.isNotEmpty(roleType)) {
            List<StaffRefDataJobTitle> refDataJobTitles = roleType.stream()
                    .map(StaffRefDataJobTitle::new)
                    .toList();
            staffRefJobTitleResponseBuilder.jobTitles(refDataJobTitles);
            log.debug("refDataJobTitles = {}", refDataJobTitles);
            return   ResponseEntity
                    .status(200)
                    .body(staffRefJobTitleResponseBuilder.build());
        } else {
            log.error("Record not found ");
            return ResponseEntity.status(404).body(roleType);
        }
    }
}
