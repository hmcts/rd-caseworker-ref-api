
package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Default endpoints per application.
 */


@Slf4j
@RestController
public class WelcomeController {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

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
    @GetMapping(
        path = "/",
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> welcome() {

        log.info("{}:: Welcome '{}' from running instance: {}", loggingComponentName, MESSAGE, INSTANCE_ID);

        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .body("{\"message\": \"" + MESSAGE + "\"}");
    }

    @ApiOperation(
            value = "Welcome message for the Caseworker Ref Data API",
            authorizations = {
                    @Authorization(value = "ServiceAuthorization"),
                    @Authorization(value = "Authorization")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Welcome message",
                    response = String.class
            )
    })
    @GetMapping(
            path = "/welcome",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    //@Secured("cwd-admin")
    public ResponseEntity<String> welcomeWithToken() {
        log.info("{}:: Welcome '{}' from running instance: {}", loggingComponentName, MESSAGE, INSTANCE_ID);
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noCache())
                .body("{\"message\": \"" + MESSAGE + "\"}");
    }
}

