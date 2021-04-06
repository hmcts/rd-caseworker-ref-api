package uk.gov.hmcts.reform.cwrdapi;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;

//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CaseWorkerSuspendUserWithFileUploadTest extends FileUploadTest {

    String exceptedSuspenseResponse = "{\"message\":\"Request Completed Successfully\","
        + "\"message_details\":\"%s record(s) suspended\"}";

    String expectedSuspendFailureResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"%s record(s) failed validation\","
            + "\"error_details\":[{\"row_id\":\"%s\",\"error_description\":\"An update to the user is not possible"
            + " at this moment. Please try again later.\"}]}";

    @Test
    public void shouldCreateCaseWorkerUpdateAuditSuccess() throws Exception {
        validateAuditCaseWorkerCreate();
        modifyUserStatus(200);
        response = uploadCaseWorkerFile("Staff Data Upload Suspended.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(exceptedSuspenseResponse, 1),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerUpdateAuditFailureWithWrongStatus() throws Exception {
        validateAuditCaseWorkerCreate();
        modifyUserStatus(400);
        response = uploadCaseWorkerFile("Staff Data Upload Suspended.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(expectedSuspendFailureResponse, 1, 2),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerUpdateAuditExceptionFailure() throws Exception {
        validateAuditCaseWorkerCreate();
        userProfileService.stubFor(put(urlPathMatching("/v1/userprofile.*"))
            .willReturn(null));
        response = uploadCaseWorkerFile("Staff Data Upload Suspended.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(expectedSuspendFailureResponse, 1, 2),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerAuditFailureForSuspendingNewUser() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Suspended.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        assertThat(exceptionCaseWorkers.size()).isEqualTo(1);
        assertEquals(format(CaseWorkerConstants.NO_USER_TO_SUSPEND, 2),
            exceptionCaseWorkers.get(0).getErrorDescription());
    }
}
