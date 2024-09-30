package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffRefUpdateProfileFunctionalTest extends AuthorizationFunctionalTest {

    public static final String UPDATE_STAFF_PROFILE = "StaffRefDataController.updateStaffUserProfile";

    public static final String STAFF_PROFILE_URL = "/refdata/case-worker";

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_update_staff_profile_and_returns_status_200() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);



        String firstNameUpdated = "StaffProfilefirstNameChanged";
        String lastNameUpdated = "StaffProfilelastNameChanged";

        staffProfileCreationRequest.setFirstName(firstNameUpdated);
        staffProfileCreationRequest.setLastName(lastNameUpdated);

        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        StaffProfileCreationResponse staffProfileResponse =
                response.getBody().as(StaffProfileCreationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(staffProfileResponse).isNotNull();
        assertThat(staffProfileResponse.getCaseWorkerId()).isNotBlank();

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
        assertThat(response.getBody().asString()).contains("Access Denied");

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updateStaffProfileForUserPresentInUserProfileAndIdam() throws JsonProcessingException {

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

        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");
        response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);


        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile are same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updateStaffProfileDifferentThanUserPresentInUserProfileAndIdam() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);

        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);
        //Step 3: create user in SRD with updated first name and last name
        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");

        response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);


        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile are same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updateStaffProfileDifferentThanUserPresentInUserProfileAndIdamAndFlags() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);

        caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);
        //Step 3: create user in SRD with updated first name and last name
        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");
        staffRequest.setSuspended(true);

        //Step 3: create user in SRD with updated first name and last name
        Response response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);


        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile are not same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("SUSPENDED",upResponse.getIdamStatus());

        assertEquals("true", caseWorkerProfile.getSuspended());

    }


    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updateStaffProfileDelStaffAdminRoleWhenStaffAdminIsFalse() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        staffRequest.setStaffAdmin(true);
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);

        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);

        //Verify idam profile roles has staff admin
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);
        String cwId = staffProfileCreationResponse.getCaseWorkerId();
        var idamResponse = idamOpenIdClient.getUserByUserID(cwId);
        assertEquals(staffRequest.getEmailId(), idamResponse.get("email"));
        assertTrue(((List)idamResponse.get("roles")).contains(ROLE_STAFF_ADMIN));
        assertTrue(((List)idamResponse.get("roles")).contains(CWD_USER));

        //Step 3: create user in SRD with staff admin false
        staffRequest.setStaffAdmin(false);
        response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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

        // validate Idam user doesn't have staff admin role
        idamResponse = idamOpenIdClient.getUserByUserID(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        assertFalse(((List)idamResponse.get("roles")).contains(ROLE_STAFF_ADMIN));
        assertFalse(((List)idamResponse.get("roles")).isEmpty());
        assertTrue(((List)idamResponse.get("roles")).contains(CWD_USER));

        idamOpenIdClient.getcwdAdminOpenIdToken("cwd-admin");
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertFalse(upResponse.getRoles().contains(ROLE_STAFF_ADMIN));
        assertFalse((upResponse.getRoles()).isEmpty());
        assertTrue(upResponse.getRoles().contains(CWD_USER));

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void updateStaffProfileDelStaffAdminRoleDirectlyFromIdamAndStaffAdminIsTrue() throws JsonProcessingException {

        StaffProfileCreationRequest staffRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        staffRequest.setStaffAdmin(true);
        //Step 1: create user in IDM for active status
        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,staffRequest.getEmailId(),
                staffRequest.getFirstName(),staffRequest.getFirstName());
        //Step 2: create user in UP
        UserProfileCreationRequest userProfileRequest = caseWorkerApiClient.createUserProfileRequest(staffRequest);
        createUserProfileFromUp(userProfileRequest);

        Response response = caseWorkerApiClient.createStaffUserProfileWithOutIdm(staffRequest);

        //Verify idam profile roles has staff admin
        StaffProfileCreationResponse staffProfileCreationResponse =
                response.getBody().as(StaffProfileCreationResponse.class);
        String cwId = staffProfileCreationResponse.getCaseWorkerId();
        var idamResponse = idamOpenIdClient.getUserByUserID(cwId);
        assertEquals(staffRequest.getEmailId(), idamResponse.get("email"));
        assertTrue(((List)idamResponse.get("roles")).contains(ROLE_STAFF_ADMIN));
        assertTrue(((List)idamResponse.get("roles")).contains(CWD_USER));

        idamOpenIdClient.deleteRoleByUserIdNRoleName(cwId,ROLE_STAFF_ADMIN);
        idamResponse = idamOpenIdClient.getUserByUserID(cwId);
        assertEquals(staffRequest.getEmailId(), idamResponse.get("email"));
        assertFalse(((List)idamResponse.get("roles")).contains(ROLE_STAFF_ADMIN));
        assertTrue(((List)idamResponse.get("roles")).contains(CWD_USER));

        //Step 3: create user in SRD with staff admin false
        response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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

        // validate Idam user doesn't have staff admin role
        idamResponse = idamOpenIdClient.getUserByUserID(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        assertTrue(((List)idamResponse.get("roles")).contains(ROLE_STAFF_ADMIN));
        assertFalse(((List)idamResponse.get("roles")).isEmpty());
        assertTrue(((List)idamResponse.get("roles")).contains(CWD_USER));

        idamOpenIdClient.getcwdAdminOpenIdToken("cwd-admin");
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertTrue(upResponse.getRoles().contains(ROLE_STAFF_ADMIN));
        assertFalse((upResponse.getRoles()).isEmpty());
        assertTrue(upResponse.getRoles().contains(CWD_USER));
    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
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

        staffRequest.setFirstName(staffRequest.getFirstName() + "updated");
        staffRequest.setLastName(staffRequest.getLastName() + "updated");

        response = caseWorkerApiClient.updateStaffUserProfile(staffRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());

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
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);


        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(cwId);
        assertEquals(caseWorkerProfile.getId(), idamResponse.get("id"));
        assertEquals(caseWorkerProfile.getFirstName(), idamResponse.get("forename"));
        assertEquals(caseWorkerProfile.getLastName(), idamResponse.get("surname"));
        assertEquals(caseWorkerProfile.getOfficialEmail(), idamResponse.get("email"));
        // validate SRD user and UserProfile are same
        UserProfileResponse upResponse = getUserProfileFromUp(caseWorkerProfile.getOfficialEmail());
        assertEquals(caseWorkerProfile.getId(), upResponse.getIdamId());
        assertEquals(caseWorkerProfile.getFirstName(), upResponse.getFirstName());
        assertEquals(caseWorkerProfile.getLastName(), upResponse.getLastName());
        assertEquals(caseWorkerProfile.getOfficialEmail(), upResponse.getEmail());
        assertEquals("ACTIVE",upResponse.getIdamStatus());
        assertEquals("200",upResponse.getIdamStatusCode());

    }


    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_update_staff_profile_for_suspended_user_and_returns_status_200() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();
        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);
        staffProfileCreationRequest.setSuspended(true);

        StaffProfileCreationResponse staffProfileResponse1 = response.getBody().as(StaffProfileCreationResponse.class);
        assertThat(staffProfileResponse1).isNotNull();

        assertThat(staffProfileCreationRequest.isSuspended()).isTrue();
        assertThat(staffProfileCreationRequest.getFirstName()).isEqualTo("StaffProfilefirstName");
        assertThat(staffProfileCreationRequest.getLastName()).isEqualTo("StaffProfilelastName");
        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        StaffProfileCreationResponse staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(staffProfileResponse.getCaseWorkerId());
        assertFalse((Boolean) idamResponse.get("active"));
        assertEquals(staffProfileResponse.getCaseWorkerId(), idamResponse.get("id"));
        assertEquals(staffProfileCreationRequest.getFirstName(), idamResponse.get("forename"));
        assertEquals(staffProfileCreationRequest.getLastName(), idamResponse.get("surname"));
        assertEquals(staffProfileCreationRequest.getEmailId(), idamResponse.get("email"));

        // validate SRD user and UserProfile are same
        //idamOpenIdClient.getUserByUserID("cwd-admin");
        UserProfileResponse upResponse = getUserProfileFromUp(staffProfileCreationRequest.getEmailId());
        assertEquals("SUSPENDED",upResponse.getIdamStatus());


        String firstNameUpdated = "StaffProfilefirstNameChanged";
        String lastNameUpdated = "StaffProfilelastNameChanged";
        staffProfileCreationRequest.setFirstName(firstNameUpdated);
        staffProfileCreationRequest.setLastName(lastNameUpdated);

        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(staffProfileResponse).isNotNull();
        assertThat(staffProfileResponse.getCaseWorkerId()).isNotBlank();

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_update_staff_profile_for_Unsuspend_a_suspend_user_and_returns_status_200() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileResponse1 = response.getBody().as(StaffProfileCreationResponse.class);
        assertThat(staffProfileResponse1).isNotNull();

        assertThat(staffProfileCreationRequest.isSuspended()).isFalse();
        assertThat(staffProfileCreationRequest.getFirstName()).isEqualTo("StaffProfilefirstName");
        assertThat(staffProfileCreationRequest.getLastName()).isEqualTo("StaffProfilelastName");
        staffProfileCreationRequest.setSuspended(true);
        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        StaffProfileCreationResponse staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(staffProfileResponse.getCaseWorkerId());
        assertFalse((Boolean) idamResponse.get("active"));
        assertEquals(staffProfileResponse.getCaseWorkerId(), idamResponse.get("id"));
        assertEquals(staffProfileCreationRequest.getFirstName(), idamResponse.get("forename"));
        assertEquals(staffProfileCreationRequest.getLastName(), idamResponse.get("surname"));
        assertEquals(staffProfileCreationRequest.getEmailId(), idamResponse.get("email"));

        // validate SRD user and UserProfile are same
        //idamOpenIdClient.getUserByUserID("cwd-admin");
        UserProfileResponse upResponse = getUserProfileFromUp(staffProfileCreationRequest.getEmailId());
        assertEquals("SUSPENDED",upResponse.getIdamStatus());

        // UnSuspend an User
        staffProfileCreationRequest.setSuspended(false);

        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(staffProfileResponse).isNotNull();
        assertThat(staffProfileResponse.getCaseWorkerId()).isNotBlank();
        //Verify Unsuspend User in UserProfile
        UserProfileResponse upResponse1 = getUserProfileFromUp(staffProfileCreationRequest.getEmailId());
        assertEquals("ACTIVE",upResponse1.getIdamStatus());

        //Verify Unsuspend User in Idam
        var idamResponse1 = getIdamResponse(staffProfileResponse.getCaseWorkerId());
        assertTrue((Boolean) idamResponse1.get("active"));
        assertEquals(staffProfileResponse.getCaseWorkerId(), idamResponse1.get("id"));
        assertEquals(staffProfileCreationRequest.getFirstName(), idamResponse1.get("forename"));
        assertEquals(staffProfileCreationRequest.getLastName(), idamResponse1.get("surname"));
        assertEquals(staffProfileCreationRequest.getEmailId(), idamResponse1.get("email"));

    }

    @Test
    @ToggleEnable(mapKey = UPDATE_STAFF_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void should_update_staff_profile_for_Suspend_Not_SetIn_Request_Profile_status_Suspend() {

        StaffProfileCreationRequest staffProfileCreationRequest = caseWorkerApiClient
                .createStaffProfileCreationRequest();

        Response response = caseWorkerApiClient.createStaffUserProfile(staffProfileCreationRequest);

        StaffProfileCreationResponse staffProfileResponse1 = response.getBody().as(StaffProfileCreationResponse.class);
        assertThat(staffProfileResponse1).isNotNull();

        assertThat(staffProfileCreationRequest.isSuspended()).isFalse();
        assertThat(staffProfileCreationRequest.getFirstName()).isEqualTo("StaffProfilefirstName");
        assertThat(staffProfileCreationRequest.getLastName()).isEqualTo("StaffProfilelastName");
        staffProfileCreationRequest.setSuspended(true);
        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        StaffProfileCreationResponse staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        // validate SRD user and IDM user are same
        var idamResponse = getIdamResponse(staffProfileResponse.getCaseWorkerId());
        assertFalse((Boolean) idamResponse.get("active"));
        assertEquals(staffProfileResponse.getCaseWorkerId(), idamResponse.get("id"));
        assertEquals(staffProfileCreationRequest.getFirstName(), idamResponse.get("forename"));
        assertEquals(staffProfileCreationRequest.getLastName(), idamResponse.get("surname"));
        assertEquals(staffProfileCreationRequest.getEmailId(), idamResponse.get("email"));

        // validate SRD user and UserProfile are same
        //idamOpenIdClient.getUserByUserID("cwd-admin");
        UserProfileResponse upResponse = getUserProfileFromUp(staffProfileCreationRequest.getEmailId());
        assertEquals("SUSPENDED",upResponse.getIdamStatus());

        // UnSuspend an User
        staffProfileCreationRequest.setSuspended(null);

        response = caseWorkerApiClient.updateStaffUserProfile(staffProfileCreationRequest);
        staffProfileResponse = response.getBody().as(StaffProfileCreationResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(staffProfileResponse).isNotNull();
        assertThat(staffProfileResponse.getCaseWorkerId()).isNotBlank();
        //Verify Unsuspend User in UserProfile
        UserProfileResponse upResponse1 = getUserProfileFromUp(staffProfileCreationRequest.getEmailId());
        assertEquals("SUSPENDED",upResponse1.getIdamStatus());

        //Verify Unsuspend User in Idam

        var idamResponse1 = getIdamResponse(staffProfileResponse.getCaseWorkerId());
        assertFalse((Boolean) idamResponse1.get("active"));
        assertEquals(staffProfileResponse.getCaseWorkerId(), idamResponse1.get("id"));
        assertEquals(staffProfileCreationRequest.getFirstName(), idamResponse1.get("forename"));
        assertEquals(staffProfileCreationRequest.getLastName(), idamResponse1.get("surname"));
        assertEquals(staffProfileCreationRequest.getEmailId(), idamResponse1.get("email"));

        String firstCaseworkerId = staffProfileResponse.getCaseWorkerId();

        // Fetching cwp from db to verify the profile suspend status not changed when suspend flag is not set in Request

        IdamOpenIdClient.cwdStaffAdminUserToken = null;
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_STAFF_ADMIN)
                .get(STAFF_PROFILE_URL  + "/profile/search-by-id?id=" + firstCaseworkerId)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        SearchStaffUserByIdResponse caseWorkerProfile =
                fetchResponse.getBody().as(SearchStaffUserByIdResponse.class);

        assertThat(caseWorkerProfile.isSuspended()).isTrue();

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