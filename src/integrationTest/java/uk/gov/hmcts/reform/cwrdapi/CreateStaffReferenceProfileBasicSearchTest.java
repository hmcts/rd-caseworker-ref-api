package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;

public class CreateStaffReferenceProfileBasicSearchTest extends AuthorizationEnabledIntegrationTest {

    public static final String SEARCH_STAFF_USER_BY_NAME_KEY = "StaffRefDataController.searchStaffUserByName";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@justice.gov.uk";
    public static final String CWD_USER = "cwd-user";
    public static final String CASEWORKER_IAC_BULKSCAN = "caseworker-iac-bulkscan";
    public static final String CASEWORKER_IAC = "caseworker-iac";
    public static final String CASEWORKER_SENIOR_IAC = "caseworker-senior-iac";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String ROLE_CWD_ADMIN = "cwd-admin";

    public static final String ROLE_STAFF_ADMIN = "staff-admin";

    public static List<String> emailsTobeDeleted = new ArrayList<>();

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests;

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
    public void cleanUpEach(){
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

        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        createCaseWorkerTestData("sbn-James", "sbn-Smith",email);

//        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
//        createCaseWorkerTestData("sbn-Michael", "sbn-Smith",email);
//        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
//        createCaseWorkerTestData("sbn-Maria", "sbn-Garcia",email);
//        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
//        createCaseWorkerTestData("sbn-Ron", "sbn-David",email);
//        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
//        createCaseWorkerTestData("sbn-Mary", "sbn-David",email);

        String searchString = "sbn";

        String path = "/profile/search-by-name";
        String role = "staff-admin";
        //String searchString = "cwr-test";

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);

        Object response =  caseworkerReferenceDataClient
                .searchStaffUserByName(path,searchString,"1","1",role);

        assertThat(response).isNotNull();

        //assertThat(response).containsEntry("http_status", "200");
        //assertThat(response.get("response_body").toString()).contains(PAGE_SIZE + " is invalid");

  /*              .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);


        int totalRecords = Integer.valueOf(fetchResponse.getHeader("total-records"));

        assertThat(totalRecords).isGreaterThan(0);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                fetchResponse.getBody().as(SearchStaffUserResponse[].class));
        assertThat(searchStaffUserResponse).isNotNull();
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains(searchString);
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains(searchString);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");*/
    }



     void createCaseWorkerTestData(String firstName, String lastName, String email) {
        List<CaseWorkerRoleRequest> roleRequests = new ArrayList<CaseWorkerRoleRequest>();
        roleRequests.add(new CaseWorkerRoleRequest("National Business Centre Team Leader", true));
        roleRequests.add(new CaseWorkerRoleRequest("Regional Centre Team Leader", false));
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                createCaseWorkerProfiles(firstName, lastName, email);
        caseWorkersProfileCreationRequests.get(0).setRoles(roleRequests);
        //Response response = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        Map<String, Object> response = caseworkerReferenceDataClient.createCaseWorkerProfile(caseWorkersProfileCreationRequests, ROLE_CWD_ADMIN);
        assertThat(response).containsEntry("http_status", "201 CREATED");

    }

    public static List<CaseWorkersProfileCreationRequest> createCaseWorkerProfiles(String firstName,
                                                                            String lastName,String email) {
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

        String emailToUsed =  nonNull(email) ? email : generateRandomEmail();
       // setEmailsTobeDeleted(emailToUsed.toLowerCase());
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

    @Test
    public void shouldCreateCaseWorker() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");
        assertThat(response).containsEntry("http_status", "201 CREATED");

        assertThat(caseWorkerProfileRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerLocationRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerWorkAreaRepository.count()).isEqualTo(1L);
        Set<String> emails = new HashSet<>();
        emails.add("test.inttest@hmcts.gov.uk");
        CaseWorkerProfile profile =
                caseWorkerProfileRepository.findByEmailIdIn(emails).get(0);
        assertThat(profile.getFirstName()).isEqualTo("firstName");
        assertThat(profile.getLastName()).isEqualTo("lastName");
        assertThat(profile.getEmailId()).isEqualTo("test.inttest@hmcts.gov.uk");
        assertThat(profile.getRegion()).isEqualTo("region");
        assertFalse(profile.getCaseAllocator());
        assertTrue(profile.getTaskSupervisor());

        List<CaseWorkerLocation> caseWorkerLocations = caseWorkerLocationRepository.findAll();
        CaseWorkerLocation caseWorkerLocation = caseWorkerLocations.get(0);
        assertThat(caseWorkerLocation.getLocation()).isEqualTo("location");

        List<CaseWorkerWorkArea> caseWorkerWorkAreas = caseWorkerWorkAreaRepository.findAll();
        CaseWorkerWorkArea caseWorkerWorkArea = caseWorkerWorkAreas.get(0);
        assertThat(caseWorkerWorkArea.getAreaOfWork()).isEqualTo("areaOfWork");
        assertThat(caseWorkerWorkArea.getServiceCode()).isEqualTo("serviceCode");
    }


    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
    }
}
