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
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.List;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffProfileCreateFunctionalTest extends AuthorizationFunctionalTest {

    public static final String CREATE_STAFF_PROFILE = "StaffRefDataController.createStaffUserProfile";

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfile() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        Assertions.assertNotNull(staffProfileCreationResponse);
        Assertions.assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfile_StaffAdmin() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        staffProfileCreationRequest.setStaffAdmin(true);
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        Assertions.assertNotNull(staffProfileCreationResponse);
        Assertions.assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfile_Skills() {

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId("1L")
                .description("training")
                .build();

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        staffProfileCreationRequest.setSkills(List.of(skillsRequest));
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        Assertions.assertNotNull(staffProfileCreationResponse);
        Assertions.assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
    }
}