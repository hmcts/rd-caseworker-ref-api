package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import static org.junit.Assert.assertEquals;

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
                ((ErrorResponse) responseEntity.getBody()).getErrorDescription());
    }

    @Test
    public void test_handle_invalid_request_exception() {
        InvalidRequestException invalidRequestException = new InvalidRequestException("Invalid Request");

        ResponseEntity<Object> responseEntity = exceptionMapper.customValidationError(invalidRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(invalidRequestException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    public void test_handle_excel_validation_exception() {
        ExcelValidationException excelValidationException = new ExcelValidationException(HttpStatus.BAD_REQUEST,
                "Excel exception");

        ResponseEntity<Object> responseEntity = exceptionMapper
                .excelValidationExceptionHandler(excelValidationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(excelValidationException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    public void test_handle_idam_role_mapping_exception() {
        IdamRolesMappingException idamRolesMappingException =
                new IdamRolesMappingException("Idam Roles Mapping Exception");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleIdamRolesMappingError(idamRolesMappingException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(idamRolesMappingException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    public void test_handle_case_worker_publish_message_exception() {
        CaseworkerMessageFailedException caseworkerMessageFailedException =
                new CaseworkerMessageFailedException("Case worker publish message error");

        ResponseEntity<Object> responseEntity = exceptionMapper
                .handleCaseWorkerPublishMessageError(caseworkerMessageFailedException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(caseworkerMessageFailedException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    public void test_handle_launchDarkly_exception() {
        ForbiddenException forbiddenException = new ForbiddenException("LD Forbidden Exception");
        ResponseEntity<Object> responseEntity = exceptionMapper.handleLaunchDarklyException(forbiddenException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(forbiddenException.getMessage(), ((ErrorResponse)responseEntity.getBody())
                .getErrorDescription());
    }
}
