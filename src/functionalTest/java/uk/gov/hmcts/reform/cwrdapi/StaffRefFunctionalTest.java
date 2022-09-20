package uk.gov.hmcts.reform.cwrdapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.caseWorkerApiClient;

import groovy.util.logging.Slf4j;
import io.restassured.response.Response;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StaffRefFunctionalTest {

    public static final String SEARCH_STAFF_USER_BY_NAME = "StaffRefDataController.searchStaffUserByName";
    public static final String CASE_WORKER_PROFILE_URL = "/refdata/case-worker/profile";

    @Test
    @ToggleEnable(mapKey = SEARCH_STAFF_USER_BY_NAME, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_return_staff_user_with_status_code_200_when_flag_enabled() {

        List<CaseWorkerRoleRequest> roleRequests = new ArrayList<>();
        roleRequests.add(new CaseWorkerRoleRequest("National Business Centre Team Leader",true));
        roleRequests.add(new CaseWorkerRoleRequest("Regional Centre Team Leader",false));
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
            .createCaseWorkerProfiles();
        caseWorkersProfileCreationRequests.get(0).setRoles(roleRequests);
        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        String SEARCH_STRING = "cwr";

        Response fetchResponse = caseWorkerApiClient
            .getMultipleAuthHeadersWithoutContentType(ROLE_STAFF_ADMIN)
            .get(CASE_WORKER_PROFILE_URL + "/search-by-name?" + SEARCH_STRING)
            .andReturn();
        fetchResponse.then()
            .assertThat()
            .statusCode(200);

        List<SearchStaffUserResponse> searchStaffUserResponse = Arrays.asList(
            fetchResponse.getBody().as(SearchStaffUserResponse[].class));
        assertThat(searchStaffUserResponse).isNotNull().hasSize(1);
        assertThat(searchStaffUserResponse.get(0).getFirstName()).contains(SEARCH_STRING);
        assertThat(searchStaffUserResponse.get(0).getLastName()).contains(SEARCH_STRING);

    }

}
