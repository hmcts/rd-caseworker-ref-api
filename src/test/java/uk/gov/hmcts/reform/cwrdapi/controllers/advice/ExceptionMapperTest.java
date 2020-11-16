package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperTest {

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    public void test_handle_empty_result_exception() {
        EmptyResultDataAccessException emptyResultDataAccessException = new EmptyResultDataAccessException(1);

        ResponseEntity<Object> responseEntity
                = exceptionMapper.handleEmptyResultDataAccessException(emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(emptyResultDataAccessException.getMessage(),
                ((ErrorResponse)responseEntity.getBody()).getErrorDescription());
    }

    @Test
    public void test_handle_illegal_argument_exception() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<Object> responseEntity = exceptionMapper.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }

    @Test
    public void test_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.httpMessageNotReadableExceptionError(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }


    @Test
    public void test_handle_forbidden_error_exception() {
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleForbiddenException(exception);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }

    @Test
    public void test_handle_http_status_code_exception() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        HttpStatus httpStatus = mock(HttpStatus.class);

        when(exception.getStatusCode()).thenReturn(httpStatus);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleHttpStatusException(exception);
        assertNotNull(responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }

    @Test
    public void test_handle_exception() {
        Exception exception = new Exception();

        ResponseEntity<Object> responseEntity = exceptionMapper.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse)responseEntity.getBody()).getErrorDescription());

    }

    @Test
    public void test_handle_invalid_request_exception() {
        InvalidRequestException invalidRequestException = new InvalidRequestException("Invalid Request");

        ResponseEntity<Object> responseEntity = exceptionMapper.customValidationError(invalidRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(invalidRequestException.getMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    public void test_handle_duplicate_key_exception() {
        DuplicateKeyException duplicateKeyException = new DuplicateKeyException("Duplicate Key Exception");

        ResponseEntity<Object> responseEntity = exceptionMapper.duplicateKeyException(duplicateKeyException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(duplicateKeyException.getMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorDescription());

    }
}
