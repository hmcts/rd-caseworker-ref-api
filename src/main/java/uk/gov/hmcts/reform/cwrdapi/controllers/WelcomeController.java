
package uk.gov.hmcts.reform.cwrdapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
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

    @Autowired
    ExcelValidatorServiceImpl excelValidatorService;

    @Autowired
    ExcelAdaptorServiceImpl excelAdaptorService;



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

    @PostMapping(
            path = "/upload-file11",
            consumes = "multipart/form-data",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> uploadCaseWorkerProfile(
            @RequestPart(value = "file",required = true) MultipartFile file) {


        Workbook workbook = excelValidatorService.validateExcelFile(file);

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noCache())
                .body("{\"message\": \"" + MESSAGE + "\"}");
    }

    @RequestMapping(value = "/upload-file", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        Workbook workbook = excelValidatorService.validateExcelFile(file);

        if (containsIgnoreCase(file.getOriginalFilename(), "caseworker")) {
            excelAdaptorService.parseExcel(workbook, CaseWorkerProfile.class);
        }

        return ResponseEntity
                .ok().build();

    }
}

