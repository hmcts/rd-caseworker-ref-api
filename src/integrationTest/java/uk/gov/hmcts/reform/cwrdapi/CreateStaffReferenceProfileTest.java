package uk.gov.hmcts.reform.cwrdapi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateStaffReferenceProfileTest extends AuthorizationEnabledIntegrationTest {

    public static final String CREATE_STAFF_PROFILE = "StaffRefDataController.createStaffUserProfile";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@justice.gov.uk";
    public static final String ROLE_CWD_ADMIN = "cwd-admin";

    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    CaseWorkerReferenceDataClient caseWorkerReferenceDataClient;
    @Autowired
    CaseWorkerSkillRepository caseWorkerSkillRepository;
    @Autowired
    StaffAuditRepository staffAuditRepository;

    @BeforeEach
    public void setUpClient() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        super.setUpClient();
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
        caseWorkerSkillRepository.deleteAll();
        staffAuditRepository.deleteAll();
    }

    @AfterEach
    public void cleanUpEach() {
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
        caseWorkerSkillRepository.deleteAll();
        staffAuditRepository.deleteAll();
    }

    @BeforeAll
    public static void setup() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @AfterAll
    public static void tearDown() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    void should_return_staff_user_with_status_code_201() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response).containsEntry("http_status", "201 CREATED");
    }

    @Test
    void should_return_staff_user_with_status_code_201_child_tables_size() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response).containsEntry("http_status", "201 CREATED");
        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(1);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(2);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(1);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(2);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(3);
        assertThat(staffAuditRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void should_return_staff_user_with_status_code_staff_audit() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response).containsEntry("http_status", "201 CREATED");
       
    }
}
