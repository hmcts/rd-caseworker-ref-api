package uk.gov.hmcts.reform.cwrdapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class CaseWorkerProfileRepositorySaveIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;


    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
    }

    @Test
    void should_not_save_caseworker_profile_when_same_location_name_same_locationId() {
        CaseWorkerProfile caseWorkerProfile = caseworkerReferenceDataClient.createCaseWorkerProfile("234873",
                1, "National 1", 1, "National 1");
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class,
                () ->  caseWorkerProfileRepository.saveAndFlush(caseWorkerProfile));
        assertNotNull(exception);
        assertTrue(exception.getCause().getCause().getMessage().contains("duplicate key value violates unique constraint \"case_worker_locn_id_uq\""));
    }
}
