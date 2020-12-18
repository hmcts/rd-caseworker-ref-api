package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


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

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> customValidationError(
        InvalidRequestException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, ErrorConstants.INVALID_REQUEST_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<Object> excelValidationExceptionHandler(
            ExcelValidationException ex) {
        return errorDetailsResponseEntity(ex, ex.getHttpStatus(), ex.getErrorMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handlerForNoCaseWorkersFound(
            ResourceNotFoundException ex) {
        return errorDetailsResponseEntity(ex, NOT_FOUND, ErrorConstants.EMPTY_RESULT_DATA_ACCESS.getErrorMessage());
    }

    @ExceptionHandler(IdamRolesMappingException.class)
    public ResponseEntity<Object> handleIdamRolesMappingError(
            IdamRolesMappingException ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(CaseworkerMessageFailedException.class)
    public ResponseEntity<Object> handleCaseWorkerPublishMessageError(
            CaseworkerMessageFailedException ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleLaunchDarklyException(Exception ex) {
        return errorDetailsResponseEntity(ex, FORBIDDEN, ex.getMessage());
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
