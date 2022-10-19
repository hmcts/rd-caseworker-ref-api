package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.gov.hmcts.reform.cwrdapi.CaseWorkerRefFunctionalTest.DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffRefCreateFunctionalTest extends AuthorizationFunctionalTest {

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

        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
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

        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfile_Skills() {

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("training")
                .build();

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        staffProfileCreationRequest.setSkills(List.of(skillsRequest));
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());
    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = false)
    //this test verifies new User profile is created is prohibited when api is toggled off
    void createStaffProfile_LD_disabled() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN))
                .body(staffProfileCreationRequest)
                .post("/refdata/case-worker/profile")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    void createStaffProfile_CwdAdminRoleOnly() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_ADMIN)
                .body(staffProfileCreationRequest)
                .post("/refdata/case-worker/profile")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains("Access is denied");

    }

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by ID
    static void deleteStaffProfileById() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);

        //delete user
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?userId=" + caseWorkerIds, BAD_REQUEST);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(caseWorkerIds)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by Email Pattern
    static void deleteStaffProfileByEmailPattern() {
        String emailPattern = "deleteTest1234";
        String email = format(STAFF_EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                caseWorkerApiClient.createCaseWorkerProfiles(email);

        // create user with email pattern
        Response createResponse = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                createResponse.getBody().as(CaseWorkerProfileCreationResponse.class);

        List<String> caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());

        //delete user by email pattern
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?emailPattern=" + "staff-profile-func-test-user", NO_CONTENT);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @AfterAll
    public static void cleanUpTestData() {
        try {
            deleteStaffProfileById();
            deleteStaffProfileByEmailPattern();
        } catch (Exception e) {
            log.error("cleanUpTestData :: threw the following exception: " + e);
        }
    }
}