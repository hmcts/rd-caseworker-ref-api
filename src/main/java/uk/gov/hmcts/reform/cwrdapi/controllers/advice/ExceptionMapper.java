package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.CONFLICT_EXCEPTION;


@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.cwrdapi.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private static final String HANDLING_EXCEPTION_TEMPLATE = "{}:: handling exception: {}";

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex) {
        return errorDetailsResponseEntity(ex, NOT_FOUND, ErrorConstants.EMPTY_RESULT_DATA_ACCESS.getErrorMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, ErrorConstants.INVALID_REQUEST_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleHttpStatusException(HttpStatusCodeException ex) {
        HttpStatus httpStatus = ex.getStatusCode();
        return errorDetailsResponseEntity(ex, httpStatus, httpStatus.getReasonPhrase());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> duplicateKeyException(
        DuplicateKeyException ex) {
        return errorDetailsResponseEntity(ex, CONFLICT, CONFLICT_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> customValidationError(
        InvalidRequestException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, ErrorConstants.INVALID_REQUEST_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> httpMessageNotReadableExceptionError(HttpMessageNotReadableException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, ErrorConstants.MALFORMED_JSON.getErrorMessage());

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return errorDetailsResponseEntity(ex,INTERNAL_SERVER_ERROR, ErrorConstants.UNKNOWN_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleForbiddenException(Exception ex) {
        return errorDetailsResponseEntity(ex, FORBIDDEN, ErrorConstants.ACCESS_EXCEPTION.getErrorMessage());
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
        ErrorResponse errorDetails = new ErrorResponse(httpStatus.value(),httpStatus.getReasonPhrase(),errorMsg,
                                                       getRootException(ex).getLocalizedMessage(),
                getTimeStamp());

        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}
