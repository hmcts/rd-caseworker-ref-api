package uk.gov.hmcts.reform.cwrdapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CreateCaseWorkerProfileIntTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void persists_and_returns_valid_organisation_with_contact_and_dxAddress() {
        CaseWorkersProfileCreationRequest creationRequest = new CaseWorkersProfileCreationRequest();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(creationRequest, cwrdAdmin);
        assertThat(response);

    }



}