package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Condition;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.validateSearchUserProfileResponse;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;

public class StaffReferenceProfileAdvanceIntegrationSearchTest extends AuthorizationEnabledIntegrationTest {


    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@justice.gov.uk";
    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    public static final String ROLE_CWD_ADMIN = "cwd-admin";


    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    SearchRequest searchReq;

    @BeforeEach
    public void setUpClient() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        super.setUpClient();
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
    }

    @AfterEach
    public void cleanUpEach() {
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
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
    void should_return_staff_user_with_status_code_200_when_flag_enabled_default_pagination() {

        createCaseWorkerProfiles();

        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "task supervisor,case allocator,staff administrator";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(5);

        searchReq = SearchRequest.builder()
                .serviceCode("ABA1")
                .location("12345")
                .userType("1")
                .jobTitle("2")
                .role("task supervisor,case allocators,staff administrator")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_flag_enabled_with_pagination() {

        createCaseWorkerProfiles();


        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "task supervisor,case allocator,staff administrator";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .serviceCode("ABA1")
                .location("12345")
                .userType("1")
                .jobTitle("2")
                .role("task supervisor,case allocator,staff administrator")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }


    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_serviceCode() {

        createCaseWorkerProfiles();

        String searchString = "serviceCode=ABA1";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .serviceCode("ABA1")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_serviceCodes() {

        createCaseWorkerProfiles();

        String searchString = "serviceCode=ABA1,serviceCode2";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .serviceCode("ABA1,serviceCode2")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_location() {

        createCaseWorkerProfiles();
        String searchString = "location=12345";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .location("12345")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }


    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_location() {

        createCaseWorkerProfiles();


        String searchString = "location=12345,6789";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .location("12345,6789")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_userType() {

        createCaseWorkerProfiles();


        String searchString = "userType=1";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .userType("1")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_Jobtitle() {

        createCaseWorkerProfiles();


        String searchString = "jobTitle=2";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .userType("CTSC")
                .jobTitle("2")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }


    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_role() {

        createCaseWorkerProfiles();


        String searchString = "role=case allocator";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .userType("CTSC")
                .role("task supervisor")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_skill_are_empty() {

        createCaseWorkerProfiles();
        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "task supervisor,case allocator,staff administrator";

        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        assertThat(searchStaffUserResponse.get(0).getSkills()).isEmpty();

    }


    @ParameterizedTest
    @ValueSource(strings = {"serviceCode=sddd","location=127494","userType=12","jobTitle=4224",
            "role=staff administrator","skill=132"})
    void should_return_staff_user_with_status_code_200_with_empty_search_response(String searchString) {

        createCaseWorkerProfiles();
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().isEmpty();

    }


    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_list_of_roles() {

        createCaseWorkerProfiles();


        String searchString = "role=task supervisor,case allocator,staff administrator";
        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> five = new Condition<>(records -> records.equals("5"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(five, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        searchReq = SearchRequest.builder()
                .userType("CTSC")
                .role("task supervisor,case allocator,staff administrator")
                .build();
        validateSearchUserProfileResponse(response,searchReq);
    }



    @Test
    void should_return_status_code_400_when_page_size_is_zero() {
        String searchString = "serviceCode=ABA1";
        String path = "/profile/search?";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUser(path, searchString, "0", "1", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_SIZE + " is invalid");

    }

    @Test
    void should_return_status_code_400_when_page_num_is_zero() {
        String searchString = "serviceCode=ABA1";
        String path = "/profile/search?";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUser(path, searchString, "1", "0", ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_NUMBER + " is invalid");

    }

    @ParameterizedTest
    @ValueSource(strings = {"","serviceCode=*_sd","location=1adf*_","userType=1sdfs*__","jobTitle=asdfs",
            "role=task_supervisor","skill=asdfd"})
    void should_return_status_code_400(String searchString) {
        String path = "/profile/search?";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUser(path, searchString, "1", "1", ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Bad Request");

    }



    void createCaseWorkerTestData(String firstName, String lastName, String email) {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                createCaseWorkerProfiles(firstName, lastName, email);
        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, ROLE_CWD_ADMIN);
        assertThat(response).containsEntry("http_status", "201 CREATED");

    }


    public static List<CaseWorkersProfileCreationRequest> createCaseWorkerProfiles(String firstName,
                                                                                   String lastName, String email) {
        List<CaseWorkerLocationRequest> locationRequestList = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .location("test location")
                .locationId(12345).isPrimaryFlag(true).build(),
                CaseWorkerLocationRequest
                        .caseWorkersLocationRequest()
                        .location("test location")
                        .locationId(6789).isPrimaryFlag(true).build());


        List<CaseWorkerRoleRequest> roleRequests = ImmutableList.of(CaseWorkerRoleRequest
                .caseWorkerRoleRequest()
                .role("Legal Caseworker").isPrimaryFlag(true).build());


        CaseWorkerWorkAreaRequest workerWorkAreaRequest1 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("serviceCode2").areaOfWork("Immigration and Asylum Appeals").build();

        CaseWorkerWorkAreaRequest workerWorkAreaRequest2 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("ABA1").areaOfWork("Divorce").build();

        List<CaseWorkerWorkAreaRequest> areaRequests = ImmutableList
                .of(workerWorkAreaRequest1, workerWorkAreaRequest2);

        Set<String> idamRoles = new HashSet<>();

        String emailToUsed = nonNull(email) ? email : generateRandomEmail();
        return ImmutableList.of(
                CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest()
                        .firstName(firstName)
                        .lastName(lastName)
                        .emailId(emailToUsed.toLowerCase())
                        .regionId(1)
                        .region("National")
                        .userType("CTSC")
                        .suspended(false)
                        .caseAllocator(true)
                        .taskSupervisor(true)
                        .idamRoles(idamRoles)
                        .baseLocations(locationRequestList)
                        .roles(roleRequests)
                        .workerWorkAreaRequests(areaRequests).build());
    }


    private void createCaseWorkerProfiles() {

        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-James", "sbn-Smith", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10)
                + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Michael", "sbn-Smith", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10)
                + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Maria", "sbn-Garcia", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10)
                + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Ron", "sbn-David", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10)
                + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Mary", "sbn-David", email);
    }


    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
    }
}