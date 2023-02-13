package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
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
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_ROLE_PRESENT_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;

public class CreateStaffReferenceProfileIntegrationTest extends AuthorizationEnabledIntegrationTest {

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
        mockJwtToken(ROLE_STAFF_ADMIN);
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
    @DisplayName("Create Staff profile with status 201")
    void should_return_staff_user_with_status_code_201() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient.createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response)
                .isNotNull()
                .containsEntry("http_status", "201 CREATED");

        assertThat(response.get("case_worker_id")).isNotNull();
    }

    @Test
    @DisplayName("Create Staff profile with status 201 with child tables entries")
    void should_return_staff_user_with_status_code_201_child_tables_size() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response)
                .isNotNull()
                .containsEntry("http_status", "201 CREATED");

        assertThat(caseWorkerProfileRepository.count()).isEqualTo(1);
        assertThat(caseWorkerLocationRepository.count()).isEqualTo(2);
        assertThat(caseWorkerRoleRepository.count()).isEqualTo(1);
        assertThat(caseWorkerWorkAreaRepository.count()).isEqualTo(2);
        assertThat(caseWorkerSkillRepository.count()).isEqualTo(1);
        assertThat(staffAuditRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Create Staff profile with staff audit table")
    void validate_staff_audit_data() throws JsonProcessingException, JSONException {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response)
                .isNotNull()
                .containsEntry("http_status", "201 CREATED");
        assertThat(response.get("case_worker_id")).isNotNull();

        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        assertThat(staffAudits.size()).isEqualTo(1);
        assertThat(staffAudits.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isBlank();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(caseWorkerProfileRepository.count()).isEqualTo(1);
        assertThat(caseWorkerLocationRepository.count()).isEqualTo(2);
        assertThat(caseWorkerRoleRepository.count()).isEqualTo(1);
        assertThat(caseWorkerWorkAreaRepository.count()).isEqualTo(2);
        assertThat(caseWorkerSkillRepository.count()).isEqualTo(1);
        assertThat(staffAuditRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Create Staff profile with status 400 Invalid Email")
    void should_return_create_staff_user_with_status_code_400_invalid_email_id() throws Exception {

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setEmailId("testing@test.com");


        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status","400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody).contains(INVALID_EMAIL);


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);

        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getFirstName());
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getLastName());

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
        assertThat(caseWorkerSkillRepository.count()).isZero();
        assertThat(staffAuditRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Create Staff profile with status 400 for invalid primary location")
    void should_return_create_staff_user_with_status_code_400_invalid_primary_locations() throws Exception {

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
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status","400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody).contains(NO_PRIMARY_LOCATION_PRESENT_PROFILE);


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getFirstName());
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getLastName());


        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
        assertThat(caseWorkerSkillRepository.count()).isZero();
        assertThat(staffAuditRepository.count()).isEqualTo(1);

    }

    @Test
    @DisplayName("Create Staff profile with status 400 for duplicate service codes")
    void should_return_create_staff_user_with_status_code_400_duplicate_service_codes() throws Exception {

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
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status","400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody).contains(DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK);


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();

        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getFirstName());
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getLastName());

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
        assertThat(caseWorkerSkillRepository.count()).isZero();
        assertThat(staffAuditRepository.count()).isEqualTo(1);

    }

    @Test
    @DisplayName("Create staff profile with status 400 for duplicate roles")
    void should_return_create_staff_user_with_status_code_400_duplicate_roles() throws Exception {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1, "testRole", true);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1, "testRole", true);


        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setRoles(List.of(staffProfileRoleRequest1,staffProfileRoleRequest2));


        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status","400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody).contains(DUPLICATE_PRIMARY_AND_SECONDARY_ROLES);


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);

        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getFirstName());
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getLastName());

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
        assertThat(caseWorkerSkillRepository.count()).isZero();
        assertThat(staffAuditRepository.count()).isEqualTo(1);

    }

    @Test
    @DisplayName("Create staff profile with status 400 for invalid roles")
    void should_return_create_staff_user_with_status_code_400_invalid_roles() throws Exception {

        StaffProfileRoleRequest staffProfileRoleRequest1 =
                new StaffProfileRoleRequest(1, "testRole", false);
        StaffProfileRoleRequest staffProfileRoleRequest2 =
                new StaffProfileRoleRequest(1, "adminRole", false);

        StaffProfileCreationRequest request = caseWorkerReferenceDataClient.createStaffProfileCreationRequest();
        request.setRoles(List.of(staffProfileRoleRequest1,staffProfileRoleRequest2));

        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(request,ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status","400");
        String responseBody = (String) response.get("response_body");
        assertThat(responseBody).contains(NO_PRIMARY_ROLE_PRESENT_PROFILE);


        List<StaffAudit> staffAudits = staffAuditRepository.findAll();

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String expectedData = mapper.writeValueAsString(request);

        JSONAssert.assertEquals(staffAudits.get(0).getRequestLog(), expectedData, JSONCompareMode.LENIENT);


        assertThat(staffAudits.get(0).getStatus()).isEqualTo("FAILURE");
        assertThat(staffAudits.get(0).getOperationType()).isEqualTo("CREATE");
        assertThat(staffAudits.get(0).getErrorDescription()).isNotBlank();
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getFirstName());
        assertThat(staffAudits.get(0).getRequestLog()).contains(request.getLastName());

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
        assertThat(caseWorkerSkillRepository.count()).isZero();
        assertThat(staffAuditRepository.count()).isEqualTo(1);
    }
}
