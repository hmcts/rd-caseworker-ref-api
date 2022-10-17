package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.List;
import java.util.Map;

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
public class StaffRefUpdateProfileFunctionalTest extends AuthorizationFunctionalTest {

    public static final String UPDATE_STAFF_PROFILE = "StaffRefDataController.updateStaffUserProfile";


    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_update_staff_profile_and_returns_status_200() {
        String emailPattern = "sbnTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();
        List<String> roles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        //createUser(List<String> userRoles, String userEmail, String firstName, String lastName)
        String firstName = "StaffProfilefirstName";
        String lastName = "StaffProfilelastName";

        Map<String, String> userDetail = idamOpenIdClient.createUser(roles,
                email,firstName,lastName);
        String userEmail = userDetail.get(EMAIL);

        //var upResponse = getUserProfileFromUp(userEmail);

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest(userEmail,firstName,lastName);
        //Created userprofile
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        String firstNameUpdated = "StaffProfilefirstNameChanged";
        String lastNameUpdated = "StaffProfilelastNameChanged";

        staffProfileCreationRequest.setFirstName(firstNameUpdated);
        staffProfileCreationRequest.setLastName(lastNameUpdated);

        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        StaffProfileCreationResponse staffProfileResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        Assertions.assertNotNull(staffProfileResponse);
        Assertions.assertNotNull(staffProfileResponse.getCaseWorkerId());



    }




    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = false)
    void should_return_status_403_when_LD_disabled() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient
                .getMultipleAuthHeadersInternal(List.of(ROLE_CWD_ADMIN, ROLE_STAFF_ADMIN))
                .body(staffProfileCreationRequest)
                .put("/refdata/case-worker/profile")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    void should_return_status_403_when_role_is_not_staff_admin() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient
                .getMultipleAuthHeadersInternal(List.of(CWD_USER))
                .body(staffProfileCreationRequest)
                .put("/refdata/case-worker/profile")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }


}