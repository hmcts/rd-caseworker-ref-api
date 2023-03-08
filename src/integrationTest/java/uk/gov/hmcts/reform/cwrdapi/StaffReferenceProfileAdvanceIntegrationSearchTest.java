package uk.gov.hmcts.reform.cwrdapi;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.validateSearchUserProfileResponse;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;

public class StaffReferenceProfileAdvanceIntegrationSearchTest extends AuthorizationEnabledIntegrationTest {



    public static final String ROLE_STAFF_ADMIN = "staff-admin";


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
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
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
                + "task supervisor,case allocator,staff administrator&skill=9";
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
                .skill("9")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_flag_enabled_with_pagination() {

        createCaseWorkerProfiles();


        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "task supervisor,case allocator,staff administrator&skill=9";
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
                .skill("9")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
                .jobTitle("2")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
                .role("task supervisor")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_search_with_skill() {

        createCaseWorkerProfiles();


        String searchString = "skill=9";
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
                .skill("9")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
    }




    @Test
    void should_return_staff_user_with_status_code_200_when_skill_are_empty() {

        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest staffProfileCreationRequest = caseworkerReferenceDataClient
                .createStaffProfileCreationRequest();
        staffProfileCreationRequest.setSkills(null);
        Map<String, Object> staffProfileResponse = caseworkerReferenceDataClient
                .createStaffProfile(staffProfileCreationRequest, ROLE_STAFF_ADMIN);
        assertThat(staffProfileResponse).containsEntry("http_status", "201 CREATED");


        String searchString = "serviceCode=ABA1&location=12345&userType=1&jobTitle=2&role="
                + "task supervisor,case allocator,staff administrator";

        String path = "/profile/search?";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<List<SearchStaffUserResponse>> response = caseworkerReferenceDataClient
                .searchStaffUserExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();
        final List<String> totalRecords = response.getHeaders().get("total-records");
        Condition<String> one = new Condition<>(records -> records.equals("1"), "total %s check",
                "records");
        assertThat(totalRecords).isNotNull().isNotEmpty().hasSize(1).has(one, Index.atIndex(0));
        List<SearchStaffUserResponse> searchStaffUserResponse = response.getBody();
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        assertThat(searchStaffUserResponse.get(0).getSkills()).isEmpty();

    }


    @ParameterizedTest
    @ValueSource(strings = {"serviceCode=sddd", "location=127494", "userType=12", "jobTitle=4224",
            "role=staff administrator", "skill=132"})
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
                .role("task supervisor,case allocator,staff administrator")
                .build();
        assertTrue(validateSearchUserProfileResponse(response, searchReq));
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
    @ValueSource(strings = {"", "serviceCode=*_sd", "location=1adf*_", "userType=1sdfs*__", "jobTitle=asdfs",
            "role=task_supervisor", "skill=asdfd"})
    void should_return_status_code_400(String searchString) {
        String path = "/profile/search?";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUser(path, searchString, "1", "1", ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Bad Request");

    }

    public void createCaseWorkerTestData() {
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);
        StaffProfileCreationRequest staffProfileCreationRequest = caseworkerReferenceDataClient
                .createStaffProfileCreationRequest();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createStaffProfile(staffProfileCreationRequest, ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "201 CREATED");

    }


    private void createCaseWorkerProfiles() {
        IntStream.range(0, 5).forEach(i -> createCaseWorkerTestData());
    }

}