package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;




@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.cwrdapi.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private static final String HANDLING_EXCEPTION_TEMPLATE = "{}:: handling exception: ";

    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<Object> excelValidationExceptionHandler(
            ExcelValidationException ex) {
        return errorDetailsResponseEntity(ex, ex.getHttpStatus(), ex.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    private static Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

    private ResponseEntity<Object> errorDetailsResponseEntity(Exception ex, HttpStatus httpStatus, String errorMsg) {

        log.info(HANDLING_EXCEPTION_TEMPLATE, loggingComponentName, ex.getMessage(), ex);
        ErrorResponse errorDetails = new ErrorResponse(errorMsg, getRootException(ex).getLocalizedMessage(),
                getTimeStamp());

        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}
