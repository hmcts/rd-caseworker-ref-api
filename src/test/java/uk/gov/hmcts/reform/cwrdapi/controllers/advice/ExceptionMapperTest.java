package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.UNKNOWN_EXCEPTION;

@ExtendWith(MockitoExtension.class)
class ExceptionMapperTest {

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    void test_handle_empty_result_exception() {
        EmptyResultDataAccessException emptyResultDataAccessException = new EmptyResultDataAccessException(1);

        ResponseEntity<Object> responseEntity
                = exceptionMapper.handleEmptyResultDataAccessException(emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(emptyResultDataAccessException.getMessage(),
                ((ErrorResponse) responseEntity.getBody()).getErrorDescription());
    }

    @Test
    void test_handle_invalid_request_exception() {
        InvalidRequestException invalidRequestException = new InvalidRequestException("Invalid Request");

        ResponseEntity<Object> responseEntity = exceptionMapper.customValidationError(invalidRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(invalidRequestException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_excel_validation_exception() {
        ExcelValidationException excelValidationException = new ExcelValidationException(HttpStatus.BAD_REQUEST,
                "Excel exception");

        ResponseEntity<Object> responseEntity = exceptionMapper
                .excelValidationExceptionHandler(excelValidationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(excelValidationException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());
        assertFalse(excelValidationException.getErrorMessage().isEmpty());

    }

    @Test
    void test_handle_idam_role_mapping_exception() {
        IdamRolesMappingException idamRolesMappingException =
                new IdamRolesMappingException("Idam Roles Mapping Exception");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleIdamRolesMappingError(idamRolesMappingException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(idamRolesMappingException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_case_worker_publish_message_exception() {
        CaseworkerMessageFailedException caseworkerMessageFailedException =
                new CaseworkerMessageFailedException(CaseWorkerConstants.ASB_PUBLISH_ERROR);

        ResponseEntity<Object> responseEntity = exceptionMapper
                .handleCaseWorkerPublishMessageError(caseworkerMessageFailedException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(caseworkerMessageFailedException.getMessage(), responseEntity.getBody());

    }

    @Test
    void test_handle_launchDarkly_exception() {
        ForbiddenException forbiddenException = new ForbiddenException("LD Forbidden Exception");
        ResponseEntity<Object> responseEntity = exceptionMapper.handleLaunchDarklyException(forbiddenException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(forbiddenException.getMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorDescription());
    }

    @Test
    void test_handle_general_exception() {
        Exception exception = new Exception("General Exception");
        ResponseEntity<Object> responseEntity = exceptionMapper.handleException(exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorDescription());
        assertEquals(UNKNOWN_EXCEPTION.getErrorMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorMessage());
    }

    @Test
    void test_handle_forbidden_error_exception() {
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleForbiddenException(exception);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_json_feign_response_parsing_exception() {
        StaffReferenceException exception = new StaffReferenceException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Parsing exception", "Parsing exception");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleJsonFeignResponseException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(exception.getErrorMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());
        assertEquals(exception.getErrorDescription(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }
}
