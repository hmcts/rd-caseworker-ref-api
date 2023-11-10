package uk.gov.hmcts.reform.cwrdapi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
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
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_DATA_FOUND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_PRD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;

public class UpdateCaseWorkerIntegrationTest extends AuthorizationEnabledIntegrationTest {


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
    void should_return_update_staff_user_with_status_code_200_from_profile_sync() throws Exception {

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);
        request.setFirstName("StaffProfilefirstNameCN");
        request.setLastName("StaffProfilelastNameCN");

        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName(request.getFirstName())
            .lastName(request.getLastName())
            .userId(String.valueOf(createResponse.get("case_worker_id")))
            .emailId(request.getEmailId())
            .suspended(false)
            .build();

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);

        Map<String, Object> response = caseworkerReferenceDataClient
            .updateCwProfile(caseWorkersProfileUpdationRequest,ROLE_PRD_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response.get("case_worker_id")).isNotNull();
    }

    @Test
    void should_return_resource_notfound_when_passing_an_invalid_caseworkerID() throws Exception {



        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        Map<String, Object> createResponse = caseworkerReferenceDataClient
            .createStaffProfile(request,ROLE_STAFF_ADMIN);

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);

        request.setFirstName("StaffProfilefirstNameCN");
        request.setLastName("StaffProfilelastNameCN");
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName(request.getFirstName())
            .lastName(request.getLastName())
            .userId("1234")
            .emailId(request.getEmailId())
            .suspended(false)
            .build();

        Map<String, Object> response = caseworkerReferenceDataClient
            .updateCwProfile(caseWorkersProfileUpdationRequest,ROLE_PRD_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("404");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(NO_DATA_FOUND)).isTrue();

    }

    @Test
    void should_return_update_staff_user_with_status_code_400_invalid_email_id() throws Exception {



        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        Map<String, Object> createResponse = caseworkerReferenceDataClient
            .createStaffProfile(request,ROLE_STAFF_ADMIN);

        request.setFirstName("StaffProfilefirstNameCN");
        request.setLastName("StaffProfilelastNameCN");
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName(request.getFirstName())
            .lastName(request.getLastName())
            .userId(String.valueOf(createResponse.get("case_worker_id")))
            .emailId("aaaaa")
            .suspended(false)
            .build();

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);

        Map<String, Object> response = caseworkerReferenceDataClient
            .updateCwProfile(caseWorkersProfileUpdationRequest,ROLE_PRD_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(INVALID_EMAIL)).isTrue();

    }


    @Test
    void should_return_updated_staff_records_and_verify_the_fields() throws Exception {



        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        Map<String, Object> createResponse = caseworkerReferenceDataClient
            .createStaffProfile(request,ROLE_STAFF_ADMIN);

        request.setFirstName("StaffProfilefirstNameCM");
        request.setLastName("StaffProfilelastNameCM");
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName(request.getFirstName())
            .lastName(request.getLastName())
            .userId(String.valueOf(createResponse.get("case_worker_id")))
            .emailId(request.getEmailId())
            .suspended(false)
            .build();

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);

        Map<String, Object> response = caseworkerReferenceDataClient
            .updateCwProfile(caseWorkersProfileUpdationRequest,ROLE_PRD_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response.get("case_worker_id")).isNotNull();

        String searchString = "?id=" + createResponse.get("case_worker_id");
        String path = "/profile/search-by-id";
        caseworkerReferenceDataClient.setBearerToken(null);
        userProfileGetUserByIdWireMock(String.valueOf(createResponse.get("case_worker_id")), 200);
        SearchStaffUserByIdResponse getResponse =  (SearchStaffUserByIdResponse)caseworkerReferenceDataClient
            .fetchStaffUserById(SearchStaffUserByIdResponse.class, path + searchString,ROLE_STAFF_ADMIN);
        Assertions.assertEquals(caseWorkersProfileUpdationRequest.getUserId(), getResponse.getCaseWorkerId());
        Assertions.assertEquals(caseWorkersProfileUpdationRequest.getFirstName(), getResponse.getFirstName());
        Assertions.assertEquals(request.getRoles().get(0).getRole(), getResponse.getRoles().get(0).getRoleName());

    }



}