package uk.gov.hmcts.reform.cwrdapi;

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CaseWorkerUpdateUserWithFileUploadTest extends FileUploadTest {


    String expectedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"%s record(s) failed validation\","
            + "\"error_details\":[{\"row_id\":\"%s\",\"error_description\":\"An update to the user is not possible at"
            + " this moment. Please try again later.\"}]}";

    @Test
    public void shouldCreateCaseWorkerUpdateAuditSuccess() throws Exception {
        validateAuditCaseWorkerCreate();
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();

        String roles = "[\"Senior Tribunal Caseworker\"]";
        userProfileGetUserWireMock("ACTIVE", roles);
        modifyUserRoles();
        response = uploadCaseWorkerFile("Staff Data Upload Update.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(exceptedResponse, 1), CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerFailedToUpdateStaleUser() throws Exception {
        validateAuditCaseWorkerCreate();
        String roles = "[\"Senior Tribunal Caseworker\"]";
        userProfileGetUserWireMock("STALE", roles);
        modifyUserRoles();
        response = uploadCaseWorkerFile("Staff Data Upload Update.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(expectedResponse, 1, 2),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerFailedToUpdateFailedExceptionInFetchingUser() throws Exception {
        validateAuditCaseWorkerCreate();
        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
            .willReturn(null));
        modifyUserRoles();
        response = uploadCaseWorkerFile("Staff Data Upload Update.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(expectedResponse, 1, 2),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    @Test
    public void shouldCreateCaseWorkerFailedToUpdateFailedExceptionInModifyingUser() throws Exception {
        validateAuditCaseWorkerCreate();
        String roles = "[\"Senior Tribunal Caseworker\"]";
        userProfileGetUserWireMock("ACTIVE", roles);
        userProfileService.stubFor(put(urlPathMatching("/v1/userprofile.*"))
            .willReturn(null));
        response = uploadCaseWorkerFile("Staff Data Upload Update.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(expectedResponse, 1, 2),
                CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }
}
