package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_ADMIN;

public class CreateStaffReferenceProfileBasicSearchTest extends AuthorizationEnabledIntegrationTest {

    public static final String SEARCH_STAFF_USER_BY_NAME_KEY = "StaffRefDataController.searchStaffUserByName";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@justice.gov.uk";

    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

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

        generateCaseWorkerData();
        String searchString = "sbn";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);


        assertThat(response).isNotNull();


        int totalRecords = Integer.valueOf(response.getHeaders().get("total-records").get(0));

        assertThat(totalRecords).isEqualTo(5);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        validateSearchStaffUserResponse(searchStaffUserResponse);
    }

    @Test
    void should_return_staff_user_with_status_code_200_when_flag_enabled_with_pagination() {

        generateCaseWorkerData();

        String searchString = "sbn";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, "1", "1", ROLE_STAFF_ADMIN);


        assertThat(response).isNotNull();


        int totalRecords = Integer.valueOf(response.getHeaders().get("total-records").get(0));

        assertThat(totalRecords).isEqualTo(5);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");

    }

    @Test
    void should_return_staff_search_by_name_with_firstname_and_lastname_status_code_200() {

        generateCaseWorkerData();
        String searchString = "sbn-Mary sbn-David";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();

        assertThat(Integer.valueOf(response.getHeaders().get("total-records").get(0))).isEqualTo(1);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");
    }

    @Test
    void should_return_staff_search_by_name_with_firstname_and_lastname_with_phonetics_status_code_200() {

        generateCaseWorkerDataPhoneticAndSpecial();
        String searchString = "sbn-Æquen";
        List<SearchStaffUserResponse> searchStaffUserResponse = searchApiCallAndCommonAssertions(searchString);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Æquen");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-Ïndîkä");
    }

    @Test
    void should_return_staff_search_by_name_with_firstname_and_lastname_with_roman_status_code_200() {

        generateCaseWorkerDataPhoneticAndSpecial();
        String searchString = "sbn-IVXIIV";

        List<SearchStaffUserResponse> searchStaffUserResponse = searchApiCallAndCommonAssertions(searchString);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-IVXIIV");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-I.V-X&IIV");
    }

    @Test
    void should_return_staff_search_by_name_with_firstname_and_lastname_with_specialCharacters_status_code_200() {

        generateCaseWorkerDataPhoneticAndSpecial();
        String searchString = "sbn-I.V-X&IIV";
        List<SearchStaffUserResponse> searchStaffUserResponse = searchApiCallAndCommonAssertions(searchString);

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-IVXIIV");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-I.V-X&IIV");
    }

    @Test
    void should_return_staff_search_by_firstname_and_lastname_initial_status_code_200() {

        generateCaseWorkerData();
        String searchString = "s     sbn-Smith";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();

        assertThat(Integer.valueOf(response.getHeaders().get("total-records").get(0))).isEqualTo(2);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-James");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-Smith");
        assertThat(searchStaffUserResponse.get(1).getFirstName()).contains("sbn-Michael");
        assertThat(searchStaffUserResponse.get(1).getLastName()).contains("sbn-Smith");
    }

    @Test
    void should_return_staff_search_by_name_with_only_firstname_status_code_200_when_flag_enabled_default_pagination() {

        generateCaseWorkerData();
        String searchString = "sbn-Mary";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();

        assertThat(Integer.valueOf(response.getHeaders().get("total-records").get(0))).isEqualTo(1);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");
    }

    @Test
    void should_return_staff_search_by_name_with_only_lastname_status_code_200_when_flag_enabled_default_pagination() {

        generateCaseWorkerData();
        String searchString = "sbn-David";

        String path = "/profile/search-by-name";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();

        assertThat(Integer.valueOf(response.getHeaders().get("total-records").get(0))).isEqualTo(2);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");
    }

    @Test
    void should_return_status_code_400_when_page_size_is_zero()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String searchString = "cwr-test";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUserByName(path, searchString, "0", "20", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_SIZE + " is invalid");

    }

    @Test
    void should_return_status_code_400_when_page_num_is_zero()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String searchString = "cwr-test";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUserByName(path, searchString, "1", "0", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(PAGE_NUMBER + " is invalid");

    }

    @Test
    void should_return_status_code_400_when_search_String_is_not_valid()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String searchString = "1234";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUserByName(path, searchString, "0", "20", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("The field Page Size is invalid. Please provide a valid value.");

    }

    @Test
    void should_return_status_code_400_when_search_String_len_less_3()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String searchString = "ab";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUserByName(path, searchString, "0", "20", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("The search string should contain at least 3 characters.");

    }

    @Test
    void should_return_status_code_400_when_search_String_empty()
            throws JsonProcessingException {
        String path = "/profile/search-by-name";
        String searchString = "";

        Map<String, Object> response = caseworkerReferenceDataClient
                .searchStaffUserByName(path, searchString, "0", "20", ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Required request parameter 'search' for method parameter type String is not present");

    }

    private List<SearchStaffUserResponse> searchApiCallAndCommonAssertions(String searchString) {
        String path = "/profile/search-by-name";
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        ResponseEntity<SearchStaffUserResponse[]> response = caseworkerReferenceDataClient
                .searchStaffUserByNameExchange(path, searchString, null, null, ROLE_STAFF_ADMIN);

        assertThat(response).isNotNull();

        assertThat(Integer.valueOf(response.getHeaders().get("total-records").get(0))).isEqualTo(1);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                response.getBody());

        assertThat(searchStaffUserResponse).isNotNull();
        return searchStaffUserResponse;
    }

    private void generateCaseWorkerData() {
        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        createCaseWorkerTestData("sbn-James", "sbn-Smith", email);

        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Michael", "sbn-Smith", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Maria", "sbn-Garcia", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Ron", "sbn-David", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Mary", "sbn-David", email);
    }

    private void generateCaseWorkerDataPhoneticAndSpecial() {
        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Æquen", "sbn-Ïndîkä", email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-IVXIIV", "sbn-I.V-X&IIV", email);
    }

    private void validateSearchStaffUserResponse(List<SearchStaffUserResponse> searchStaffUserResponse) {

        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");

        assertThat(searchStaffUserResponse.get(1).getFirstName()).contains("sbn-Ron");
        assertThat(searchStaffUserResponse.get(1).getLastName()).contains("sbn-David");

        assertThat(searchStaffUserResponse.get(2).getFirstName()).contains("sbn-Maria");
        assertThat(searchStaffUserResponse.get(2).getLastName()).contains("sbn-Garcia");

        assertThat(searchStaffUserResponse.get(3).getFirstName()).contains("sbn-James");
        assertThat(searchStaffUserResponse.get(3).getLastName()).contains("sbn-Smith");

        assertThat(searchStaffUserResponse.get(4).getFirstName()).contains("sbn-Michael");
        assertThat(searchStaffUserResponse.get(4).getLastName()).contains("sbn-Smith");

    }

    void createCaseWorkerTestData(String firstName, String lastName, String email) {
        List<CaseWorkerRoleRequest> roleRequests = new ArrayList<CaseWorkerRoleRequest>();
        roleRequests.add(new CaseWorkerRoleRequest("National Business Centre Team Leader", true));
        roleRequests.add(new CaseWorkerRoleRequest("Regional Centre Team Leader", false));
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                createCaseWorkerProfiles(firstName, lastName, email);
        caseWorkersProfileCreationRequests.get(0).setRoles(roleRequests);

        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, ROLE_CWD_ADMIN);
        assertThat(response).containsEntry("http_status", "201 CREATED");

    }

    public static List<CaseWorkersProfileCreationRequest> createCaseWorkerProfiles(String firstName,
                                                                                   String lastName, String email) {
        List<CaseWorkerLocationRequest> locationRequestList = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .location("test location")
                .locationId(2).isPrimaryFlag(true).build());

        List<CaseWorkerRoleRequest> roleRequests = ImmutableList.of(CaseWorkerRoleRequest
                .caseWorkerRoleRequest()
                .role("Legal Caseworker").isPrimaryFlag(true).build());


        CaseWorkerWorkAreaRequest workerWorkAreaRequest1 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("BBA9").areaOfWork("Immigration and Asylum Appeals").build();

        CaseWorkerWorkAreaRequest workerWorkAreaRequest2 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("ABA1").areaOfWork("Divorce").build();

        List<CaseWorkerWorkAreaRequest> areaRequests =
                ImmutableList.of(workerWorkAreaRequest1, workerWorkAreaRequest2);

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
                        .taskSupervisor(false)
                        .idamRoles(idamRoles)
                        .baseLocations(locationRequestList)
                        .roles(roleRequests)
                        .workerWorkAreaRequests(areaRequests).build());
    }


    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
    }
}
