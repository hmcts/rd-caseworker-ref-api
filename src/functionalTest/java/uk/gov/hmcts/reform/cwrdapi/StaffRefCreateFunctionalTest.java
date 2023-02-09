package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
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
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals(201,response.statusCode());

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
        response.then()
                .assertThat()
                .statusCode(201);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        Assertions.assertNotNull(staffProfileCreationResponse);
        Assertions.assertNotNull(staffProfileCreationResponse.getCaseWorkerId());

        String cwId = staffProfileCreationResponse.getCaseWorkerId();

        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(cwId)).build())
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
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getStaffAdmin()));

        var idamResponse = getIdamResponse(cwId);
        String roles = idamResponse.get("roles").toString();
        assertTrue(roles.contains("staff-admin"));
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
                .skillId(9)
                .description("testskill1")
                .skillCode("SKILL:AAA7:TEST1")
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
        assertThat(caseWorkerProfile.getSkills().size()).isGreaterThanOrEqualTo(1);
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
        assertThat(caseWorkerProfile.getSkills().size()).isZero();
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

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by Email Pattern
    static void deleteStaffProfileByEmailPattern() {
        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);

        //delete user by email pattern
        deleteCaseWorkerProfileByEmailPattern(staffProfileCreationRequest.getEmailId());

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(caseWorkerIds)).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfileForUserPresentInUserProfileAndIdam() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);
        //Step 3: create user in SRD
        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);
        response.then()
                .assertThat()
                .statusCode(201);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);

        String cwId = staffProfileCreationResponse.getCaseWorkerId();
        //Step 4: Retrieve the user in SRD
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(cwId)).build())
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

        // validate SRD user and IDM user is same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile is same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());
    }

    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfileDifferentThanUserPresentInUserProfileAndIdam() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);
        //Step 3: create user in SRD with updated first name and last name
        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");
        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);
        response.then()
                .assertThat()
                .statusCode(201);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);

        String cwId = staffProfileCreationResponse.getCaseWorkerId();
        //Step 4: Retrieve the user in SRD
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(cwId)).build())
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

        // validate SRD user and IDM user is same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile is same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());
    }


    @Test
    @ToggleEnable(mapKey = CREATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void createStaffProfileDifferentThanUserPresentInIdam() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in SRD with updated first name and last name
        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");
        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);
        response.then()
                .assertThat()
                .statusCode(201);
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        String caseWorkerIds = staffProfileCreationResponse.getCaseWorkerId();
        assertNotNull(caseWorkerIds);

        String cwId = staffProfileCreationResponse.getCaseWorkerId();
        //Step 4: Retrieve the user in SRD
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(List.of(cwId)).build())
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

        // validate SRD user and IDM user is same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile is same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());
    }

    @AfterAll
    public static void cleanUpTestData() {
        try {
            deleteCaseWorkerProfileByEmailPattern(STAFF_EMAIL_PATTERN);
        } catch (Exception e) {
            log.error("cleanUpTestData :: threw the following exception: " + e);
        }
    }

}