package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_ROLE_PRESENT_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;

public class UpdateStaffReferenceProfileTest extends AuthorizationEnabledIntegrationTest {


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
    @Transactional
    void should_return_update_staff_user_with_status_code_200_child_tables_size() throws Exception {

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);

        String roles = "[\"Senior Legal Caseworker\"]";
        userProfileGetUserWireMock("ACTIVE", roles);
        modifyUserRoles();

        request.setFirstName("StaffProfilefirstNameCN");
        request.setLastName("StaffProfilelastNameCN");
        request.setResendInvite(false);
        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        assertThat(response.get("case_worker_id")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");


        validateUpdateCaseWorkerProfile(request.getEmailId());


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();



        assertThat(staffAudits.size()).isEqualTo(2);
        assertThat(staffAudits.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isBlank();

        assertThat(staffAudits.get(1).getStatus()).isEqualTo("SUCCESS");
        assertThat(staffAudits.get(1).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(1).getErrorDescription()).isBlank();

        assertThat(staffAudits.get(1).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(1).getRequestLog().contains(request.getLastName())).isTrue();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(1).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(1);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(2);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(1);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(2);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(1);

    }


    @Test
    void should_return_update_staff_user_with_status_code_400_invalid_email_id() throws Exception {



        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setEmailId("test@test.com");


        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(INVALID_EMAIL)).isTrue();


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);

        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(0).getRequestLog().contains(request.getLastName())).isTrue();




        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void should_return_update_staff_user_with_status_code_400_invalid_primary_locations() throws Exception {


        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(false)
                .location("testLocation")
                .locationId(1)
                .build();
        CaseWorkerLocationRequest caseWorkerLocationRequest2 = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(false)
                .location("LocationSecond")
                .locationId(1)
                .build();

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        request.setBaseLocations(List.of(caseWorkerLocationRequest,caseWorkerLocationRequest2));

        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(NO_PRIMARY_LOCATION_PRESENT_PROFILE)).isTrue();


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(0).getRequestLog().contains(request.getLastName())).isTrue();




        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void should_return_update_staff_user_with_status_code_400_duplicate_service_codes() throws Exception {

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA5")
                .service("service")
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest2 = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA5")
                .service("service")
                .build();



        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setServices(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest2));


        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK)).isTrue();


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(0).getRequestLog().contains(request.getLastName())).isTrue();




        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void should_return_update_staff_user_with_status_code_400_duplicate_roles() throws Exception {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1, "testRole", true);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1, "testRole", true);


        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setRoles(List.of(staffProfileRoleRequest1,staffProfileRoleRequest2));


        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(DUPLICATE_PRIMARY_AND_SECONDARY_ROLES)).isTrue();


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);

        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(0).getRequestLog().contains(request.getLastName())).isTrue();




        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void should_return_update_staff_user_with_status_code_400_invalid_roles() throws Exception {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1, "testRole", false);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1, "adminRole", false);


        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setRoles(List.of(staffProfileRoleRequest1,staffProfileRoleRequest2));


        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(NO_PRIMARY_ROLE_PRESENT_PROFILE)).isTrue();


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("UPDATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog().contains(request.getFirstName())).isTrue();
        assertThat(staffAudits.get(0).getRequestLog().contains(request.getLastName())).isTrue();




        assertThat(caseWorkerProfileRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerLocationRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerRoleRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerWorkAreaRepository.findAll().size()).isEqualTo(0);
        assertThat(caseWorkerSkillRepository.findAll().size()).isEqualTo(0);

    }

    @Test
    void should_return_reinvite_staff_user_with_status_code_404_profile_doesnot_exist() throws Exception {

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setEmailId("test@test.com");
        request.setResendInvite(true);

        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response.get("http_status")).isEqualTo("404");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody.contains(INVALID_PROFILE)).isTrue();

    }

    @Test
    void should_return_reinvite_staff_user_with_status_code_200_profile() throws Exception {

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        request.setResendInvite(true);

        Map<String, Object> createResponse = caseworkerReferenceDataClient.createStaffProfile(request,ROLE_STAFF_ADMIN);
        Map createBody = (Map)createResponse.get("body");
        Map<String, Object> resendResponse = caseworkerReferenceDataClient.updateStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(resendResponse).isNotNull();
        assertThat(resendResponse.get("http_status")).isEqualTo("200 OK");
        Map resendResponseBody = (Map) resendResponse.get("body");
        assertEquals(createBody.get("case_worker_id"), resendResponseBody.get("case_worker_id"));
    }

    @Test
    void should_update_IdamId_when_reinvite_staff_user_true_in_crd() throws Exception {

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        userProfilePostUserWireMockForStaffProfile(false);
        userProfilePostUserWireMockForStaffProfile(true);

        Map<String, Object> createResponse = caseworkerReferenceDataClient.createStaffProfile(request,ROLE_STAFF_ADMIN);
        request.setResendInvite(true);
        Map createBody = (Map)createResponse.get("body");
        Map<String, Object> resendResponse = caseworkerReferenceDataClient.updateStaffProfile(request,ROLE_STAFF_ADMIN);
        assertThat(resendResponse).isNotNull();
        assertThat(resendResponse.get("http_status")).isEqualTo("200 OK");
        Map resendResponseBody = (Map) resendResponse.get("body");
        assertNotEquals(createBody.get("case_worker_id"), resendResponseBody.get("case_worker_id"));

        String path = "/profile/search-by-name";
        ResponseEntity<SearchStaffUserResponse[]> fetchStaff = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, request.getFirstName(), "1", "1",
                        ROLE_STAFF_ADMIN);
        assertEquals(resendResponseBody.get("case_worker_id"), fetchStaff.getBody()[0].getCaseWorkerId());
        assertNotEquals(fetchStaff.getBody()[0].getCaseWorkerId(), createBody.get("case_worker_id"));
    }

    public StaffProfileCreationRequest getStaffProfileCreationRequest() {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1, "testRole", true);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1, "adminRole", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();
        CaseWorkerLocationRequest caseWorkerLocationRequest2 = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("LocationSecond")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest2 = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA5")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("training")
                .build();

        SkillsRequest skillsRequest2 = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("training2")
                .build();


        StaffProfileCreationRequest staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(List.of(caseWorkerServicesRequest, caseWorkerServicesRequest2))
                .baseLocations(List.of(caseWorkerLocationRequest, caseWorkerLocationRequest2))
                .roles(List.of(staffProfileRoleRequest1, staffProfileRoleRequest2))
                .skills(List.of(skillsRequest, skillsRequest2))
                .build();

        return staffProfileCreationRequest;

    }

    void validateUpdateCaseWorkerProfile(String emailId) {

        CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepository.findByEmailId(emailId);

        assertThat(caseWorkerProfile).isNotNull();

        assertThat(caseWorkerProfile.getEmailId()).isNotNull();


        assertThat(caseWorkerProfile.getFirstName()).isEqualTo("StaffProfilefirstNameCN");
        assertThat(caseWorkerProfile.getLastName()).isEqualTo("StaffProfilelastNameCN");
        assertThat(caseWorkerProfile.getRegion()).isEqualTo("National");
        assertThat(caseWorkerProfile.getSuspended()).isFalse();
        assertThat(caseWorkerProfile.getTaskSupervisor()).isTrue();
        assertThat(caseWorkerProfile.getCaseAllocator()).isTrue();

        assertThat(caseWorkerProfile.getUserAdmin()).isFalse();

        assertThat(caseWorkerProfile.getUserType().getUserTypeId()).isEqualTo(1);
        assertThat(caseWorkerProfile.getUserType().getDescription()).isEqualTo("CTSC");

        assertThat(caseWorkerProfile.getCaseWorkerRoles()).hasSize(1);
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getRoleId()).isEqualTo(2);
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getPrimaryFlag()).isTrue();

        assertThat(caseWorkerProfile.getCaseWorkerLocations()).hasSize(2);
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocationId()).isEqualTo(6789);
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocation()).isEqualTo("test location2");

        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas()).hasSize(2);
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getServiceCode()).isEqualTo("ABA1");

        assertThat(caseWorkerProfile.getCaseWorkerSkills()).hasSize(1);
        assertThat(caseWorkerProfile.getCaseWorkerSkills().get(0).getSkillId()).isEqualTo(9);


    }


}