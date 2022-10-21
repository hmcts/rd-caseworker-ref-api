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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    public static List<String> caseWorkerIds = new ArrayList<>();
    public static final String FETCH_BY_CASEWORKER_ID = "CaseWorkerRefUsersController.fetchCaseworkersById";

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
                .skillId(1)
                .skillCode("1")
                .description("testskill1")
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

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = false)
    void createStaffProfile_LD_disabled() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(
                        List.of(ROLE_CWD_ADMIN, ROLE_STAFF_ADMIN))
                .body(staffProfileCreationRequest)
                .post("/refdata/case-worker/profile")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void fetchCaseWorkerDetails() {
        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("testskill1")
                .skillCode("1")
                .build();

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        staffProfileCreationRequest.setSkills(List.of(skillsRequest));

        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        assertNotNull(response);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);
        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());

        String firstCaseworkerId = staffProfileCreationResponse.getCaseWorkerId();


        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(firstCaseworkerId)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(1, fetchedList.size());

        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile = fetchedList.get(0);


        assertEquals(firstCaseworkerId, caseWorkerProfile.getId());
        assertEquals(staffProfileCreationRequest.getFirstName(), caseWorkerProfile.getFirstName());
        assertEquals(staffProfileCreationRequest.getLastName(), caseWorkerProfile.getLastName());
        assertEquals(staffProfileCreationRequest.getEmailId(), caseWorkerProfile.getOfficialEmail());
        assertEquals(staffProfileCreationRequest.isStaffAdmin() ? "Y" : "N", caseWorkerProfile.getStaffAdmin());
        assertEquals(staffProfileCreationRequest.getBaseLocations().size(), caseWorkerProfile.getLocations().size());
        assertEquals(staffProfileCreationRequest.getBaseLocations().get(0).getLocation(),
                caseWorkerProfile.getLocations().get(0).getLocationName());
        assertEquals(staffProfileCreationRequest.getServices().size(), caseWorkerProfile.getWorkAreas().size());
        assertEquals(staffProfileCreationRequest.getServices().get(0).getService(),
                caseWorkerProfile.getWorkAreas().get(0).getAreaOfWork());
        assertEquals(staffProfileCreationRequest.getRoles().size(), caseWorkerProfile.getRoles().size());
        assertEquals(staffProfileCreationRequest.getRoles().get(0).getRole(),
                caseWorkerProfile.getRoles().get(0).getRoleName());
        assertEquals(staffProfileCreationRequest.getSkills().size(), caseWorkerProfile.getSkills().size());
        assertEquals(staffProfileCreationRequest.getSkills().get(0).getSkillId(),
                caseWorkerProfile.getSkills().get(0).getSkillId());
        assertEquals(staffProfileCreationRequest.getSkills().get(0).getSkillCode(),
                caseWorkerProfile.getSkills().get(0).getSkillCode());
        assertEquals(staffProfileCreationRequest.getSkills().get(0).getDescription(),
                caseWorkerProfile.getSkills().get(0).getDescription());
        assertEquals(staffProfileCreationRequest.getServices().size(), caseWorkerProfile.getWorkAreas().size());

    }


    @Test
    // this test verifies User profile are fetched from CWR when id matched what given in request rest should be ignored
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void fetchCaseWorkerDetailsWithEmptySkills() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        assertNotNull(staffProfileCreationRequest);
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);
        assertNotNull(response);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);
        assertNotNull(staffProfileCreationResponse);
        assertNotNull(staffProfileCreationResponse.getCaseWorkerId());

        String firstCaseworkerId = staffProfileCreationResponse.getCaseWorkerId();


        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(firstCaseworkerId)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(1, fetchedList.size());
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);

        assertEquals(firstCaseworkerId, caseWorkerProfile.getId());
        assertEquals(staffProfileCreationRequest.getFirstName(), caseWorkerProfile.getFirstName());
        assertEquals(staffProfileCreationRequest.getLastName(), caseWorkerProfile.getLastName());
        assertEquals(staffProfileCreationRequest.getEmailId(), caseWorkerProfile.getOfficialEmail());
        assertEquals(staffProfileCreationRequest.isStaffAdmin() ? "Y" : "N", caseWorkerProfile.getStaffAdmin());
        assertEquals(staffProfileCreationRequest.getBaseLocations().size(), caseWorkerProfile.getLocations().size());
        assertEquals(staffProfileCreationRequest.getBaseLocations().get(0).getLocation(),
                caseWorkerProfile.getLocations().get(0).getLocationName());
        assertEquals(staffProfileCreationRequest.getServices().size(), caseWorkerProfile.getWorkAreas().size());
        assertEquals(staffProfileCreationRequest.getServices().get(0).getService(),
                caseWorkerProfile.getWorkAreas().get(0).getAreaOfWork());
        assertEquals(staffProfileCreationRequest.getRoles().size(),
                caseWorkerProfile.getRoles().size());
        assertEquals(staffProfileCreationRequest.getRoles().get(0).getRole(),
                caseWorkerProfile.getRoles().get(0).getRoleName());
        assertEquals(staffProfileCreationRequest.getServices().size(),
                caseWorkerProfile.getWorkAreas().size());
        assertEquals(0L, caseWorkerProfile.getSkills().size());
        assertThat(caseWorkerProfile.getSkills()).isEmpty();

    }

    @Test
    // this test verifies User profile are not fetched from CWR when user is invalid
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void shouldThrowForbiddenExceptionForNonAdminRole() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("prd-admin")
                .body(UserRequest.builder().userIds(Collections.singletonList("someUUID")).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

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
                "/refdata/case-worker/users?userId=" + caseWorkerIds, NO_CONTENT);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(caseWorkerIds)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @ParameterizedTest
    @ValueSource(strings = {"deleteTest1234"})
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by Email Pattern
    static void deleteStaffProfileByEmailPattern(String emailPattern) {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);
        //delete user by email pattern
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?emailPattern=" + emailPattern,NO_CONTENT);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(caseWorkerIds)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }
}