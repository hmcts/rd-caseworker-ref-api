package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StaffRefDataFunctionalTest extends AuthorizationFunctionalTest {
    public static final String STAFF_REF_DATA_SKILL_URL = "/refdata/case-worker/skill";
    public static final String STAFF_REF_DATA_RD_STAFF_UI_KEY =
            "StaffRefDataController.retrieveAllServiceSkills";
    public static final String SEARCH_STAFF_USER_BY_NAME_KEY = "StaffRefDataController.searchStaffUserByName";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_service_skills_with_status_code_200_when_flag_true() {

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        StaffWorkerSkillResponse staffWorkerSkillResponse =
                fetchResponse.getBody().as(StaffWorkerSkillResponse.class);
        assertThat(staffWorkerSkillResponse).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills()).isNotNull();
        assertThat(staffWorkerSkillResponse.getServiceSkills().size()).isEqualTo(5);

    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_service_skills_with_status_code_403_when_flag_false() {

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(fetchResponse.statusCode());
        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = STAFF_REF_DATA_RD_STAFF_UI_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_401_when_authentication_invalid() {
        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .get(STAFF_REF_DATA_SKILL_URL)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_flag_enabled_default_pagination() {

        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        createCaseWorkerTestData("sbn-James", "sbn-Smith",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Michael", "sbn-Smith",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Maria", "sbn-Garcia",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Ron", "sbn-David",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Mary", "sbn-David",email);

        String searchString = "sbn";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)

                .andReturn();
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
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");
    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_flag_enabled_with_pagination() {

        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        createCaseWorkerTestData("sbn-James", "sbn-Smith",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Michael", "sbn-Smith",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Maria", "sbn-Garcia",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Ron", "sbn-David",email);
        email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        createCaseWorkerTestData("sbn-Mary", "sbn-David",email);

        String searchString = "sbn";
        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentTypeWithPagination(ROLE_STAFF_ADMIN,"1","1")
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)

                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);


        int totalRecords = Integer.valueOf(fetchResponse.getHeader("total-records"));

        assertThat(totalRecords).isGreaterThan(0);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                fetchResponse.getBody().as(SearchStaffUserResponse[].class));
        assertThat(searchStaffUserResponse).isNotNull();
        assertThat(searchStaffUserResponse).hasSize(1);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains(searchString);
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains(searchString);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains("sbn-Mary");
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains("sbn-David");

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_403_when_flag_false() {

        String searchString = "cwr";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)

                .andReturn();

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(fetchResponse.statusCode());
        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_401_when_un_authorized() {

        String searchString = "cwr";


        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void should_return_403_when_invalid_role() {
        String searchString = "cwr";
        Response response = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);
    }

    static void createCaseWorkerTestData(String firstName, String lastName, String email) {
        List<CaseWorkerRoleRequest> roleRequests = new ArrayList<CaseWorkerRoleRequest>();
        roleRequests.add(new CaseWorkerRoleRequest("National Business Centre Team Leader", true));
        roleRequests.add(new CaseWorkerRoleRequest("Regional Centre Team Leader", false));
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(firstName, lastName, email);
        caseWorkersProfileCreationRequests.get(0).setRoles(roleRequests);
        Response response = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);


    }


}
