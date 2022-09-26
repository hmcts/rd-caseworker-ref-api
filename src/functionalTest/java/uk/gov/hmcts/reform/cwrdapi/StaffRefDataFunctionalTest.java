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
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.Arrays;
import java.util.List;

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
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_ADMIN)
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
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_ADMIN)
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
    void should_return_staff_user_with_status_code_200_when_flag_enabled() {

        String searchString = "cwr-test";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)

                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
                fetchResponse.getBody().as(SearchStaffUserResponse[].class));
        assertThat(searchStaffUserResponse).isNotNull();
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains(searchString);
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains(searchString);

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_403_when_flag_false() {

        String searchString = "cwr-test";

        Response fetchResponse = caseWorkerApiClient
                .getMultipleAuthHeadersWithoutContentType(ROLE_CWD_ADMIN)
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)

                .andReturn();

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(fetchResponse.statusCode());
        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME_KEY, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_401_when_un_authorized() {

        String searchString = "cwr-test";


        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .get(CASE_WORKER_PROFILE_URL + "/search-by-name?search=" + searchString)
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);

    }

}
