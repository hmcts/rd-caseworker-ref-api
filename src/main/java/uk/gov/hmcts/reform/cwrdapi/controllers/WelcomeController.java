
package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Default endpoints per application.
 */


@Slf4j
@RestController
public class WelcomeController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    CaseWorkerService caseWorkerService;

    private static final String INSTANCE_ID = UUID.randomUUID().toString();
    private static final String MESSAGE = "Message for the Caseworker Ref Data API";



    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */


    @ApiOperation("Welcome message for the Caseworker Ref Data API")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Welcome message",
            response = String.class
        )
    })
    @PostMapping(
            path = "/idam-roles-mapping/",
            consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> buildIdamRoleMappings(@Valid @RequestBody List<ServiceRoleMapping>
                                                                serviceRoleMappings) {
//        List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoleAssociations =
//                caseWorkerService.buildIdamRoleMappings(serviceRoleMappings);
        return ResponseEntity.ok().build();
    }
}

