package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@SuppressWarnings("unchecked")
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Autowired
    public FuncTestRequestHandler testRequestHandler;

    @Value("${userProfUrl}")
    protected String userProfUrl;

    @Test
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
    }

    @Test
    public void whenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponse.getRoles());
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidam_Ac3() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        String email = profileCreateRequests.get(0).getEmailId();
        CaseWorkersProfileCreationRequest updatedReq = caseWorkerApiClient.updateCaseWorkerProfileRequest(email).get(0);
        Set<String> idamRole = updatedReq.getIdamRoles();
        idamRole.add(CASEWORKER_IAC);
        idamRole.add(CASEWORKER_IAC_BULKSCAN);
        updatedReq.setIdamRoles(idamRole);

        caseWorkerApiClient.createUserProfiles(Collections.singletonList(updatedReq));

        UserProfileResponse upResponse = getUserProfileFromUp(email);
        assertEquals(ImmutableList.of(CASEWORKER_IAC,CWD_USER,CASEWORKER_IAC_BULKSCAN), upResponse.getRoles());

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> cwProfiles = getUserProfilesFromCw(
                asList(upResponse.getIdamId()), 200);
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile cwProfile = cwProfiles.get(0);
        assertThat(cwProfile.getId()).isEqualTo(upResponse.getIdamId());
        assertThat(cwProfile.getFirstName()).isEqualTo(updatedReq.getFirstName());
        assertThat(cwProfile.getLastName()).isEqualTo(updatedReq.getLastName());
        assertThat(cwProfile.getOfficialEmail()).isEqualTo(updatedReq.getEmailId());
        assertThat(cwProfile.getRegionId()).isEqualTo(updatedReq.getRegionId());
        assertThat(cwProfile.getRegionName()).isEqualTo(updatedReq.getRegion());
        assertThat(cwProfile.getUserId()).isEqualTo(2);
        assertThat(cwProfile.getUserType()).isEqualTo(updatedReq.getUserType());
        assertThat(cwProfile.getDeleteFlag()).isEqualTo(String.valueOf(updatedReq.isDeleteFlag()));
        assertThat(cwProfile.getLocations()).hasSize(2);
        for (CaseWorkerLocationRequest locationRequest : updatedReq.getBaseLocations()) {
            List<Location> responseLocation = cwProfile.getLocations().stream().filter(fit ->
                    locationRequest.getLocationId().equals(fit.getBaseLocationId())).collect(Collectors.toList());
            assertThat(responseLocation).isNotEmpty().hasSize(1);
            assertThat(responseLocation.get(0).getLocationName()).isEqualTo(locationRequest.getLocation());
            assertThat(responseLocation.get(0).isPrimary()).isEqualTo(locationRequest.isPrimaryFlag());
        }
        assertThat(cwProfile.getWorkAreas()).hasSize(2);
        for (CaseWorkerWorkAreaRequest workAreaRequest : updatedReq.getWorkerWorkAreaRequests()) {
            List<WorkArea> responseWorkAreas = cwProfile.getWorkAreas().stream().filter(fit ->
                    workAreaRequest.getServiceCode().equals(fit.getServiceCode())).collect(Collectors.toList());
            assertThat(responseWorkAreas).isNotEmpty().hasSize(1);
            assertThat(responseWorkAreas.get(0).getAreaOfWork()).isEqualTo(workAreaRequest.getAreaOfWork());
        }
        assertThat(cwProfile.getRoles()).hasSize(2);
        for (CaseWorkerRoleRequest roleRequest : updatedReq.getRoles()) {
            List<Role> responseRoles = cwProfile.getRoles().stream().filter(fit ->
                    roleRequest.getRole().equals(fit.getRoleName())).collect(Collectors.toList());
            assertThat(responseRoles).isNotEmpty().hasSize(1);
            assertThat(responseRoles.get(0).getRoleId()).isNotNull();
            assertThat(responseRoles.get(0).isPrimary()).isEqualTo(roleRequest.isPrimaryFlag());
        }
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndDeleteFlagTrue_Ac4() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        profileCreateRequests.get(0).setDeleteFlag(true);
        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(USER_STATUS_SUSPENDED, upResponseForExistingUser.getIdamStatus());

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> cwProfiles = getUserProfilesFromCw(
                asList(upResponseForExistingUser.getIdamId()), 200);
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile cwProfile = cwProfiles.get(0);
        assertThat(cwProfile.getDeleteFlag()).isEqualTo("true");
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndRolesAreSame_Ac5() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponseForExistingUser.getRoles());
    }

    public List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile() {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(userEmail);

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        return caseWorkersProfileCreationRequests;
    }

    public List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> getUserProfilesFromCw(
            List<String> caseWorkerIds, int expectedResponse) {
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkerIds).log().body(true)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .then()
                .log().body(true)
                .and()
                .extract().response();
        fetchResponse.then()
                .assertThat()
                .statusCode(expectedResponse);
        return asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
    }

    public UserProfileResponse getUserProfileFromUp(String email) {
        return funcTestRequestHandler.sendGet(OK,
                "/v1/userprofile/roles?email=" + email, UserProfileResponse.class, baseUrlUserProfile);
    }

    @Test
    public void shouldGetCaseWorkerDetails() {
        //Create 2 Caseworker Users
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());
        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());

        List<String> caseWorkerIds = new ArrayList<>();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        for (CaseWorkersProfileCreationRequest request : caseWorkersProfileCreationRequests) {
            UserProfileResponse resource =
                    testRequestHandler.sendGet(HttpStatus.OK,
                            "?email=" + request.getEmailId(),
                            UserProfileResponse.class, userProfUrl + "/v1/userprofile"
                    );

            caseWorkerIds.add(resource.getIdamId());

        }

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkerIds)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(2, fetchedList.size());
        fetchedList.forEach(caseWorkerProfile ->
                assertTrue(caseWorkerIds.contains(caseWorkerProfile.getId())));
    }

    @Test
    public void shouldGetOnlyFewCaseWorkerDetails() {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(caseWorkerApiClient
                .createCaseWorkerProfiles());

        List<String> caseWorkerIds = new ArrayList<>();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        for (CaseWorkersProfileCreationRequest request : caseWorkersProfileCreationRequests) {
            UserProfileResponse resource =
                    testRequestHandler.sendGet(HttpStatus.OK,
                            "?email=" + request.getEmailId(),
                            UserProfileResponse.class, userProfUrl + "/v1/userprofile"
                    );

            caseWorkerIds.add(resource.getIdamId());

        }
        caseWorkerIds.add("randomId");

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkerIds)
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

        assertTrue(caseWorkerIds.contains(caseWorkerProfile.getId()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getFirstName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getLastName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getOfficialEmail()));

        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getLocations()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getRoles()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getWorkAreas()));

    }

    @Test
    public void shouldThrowForbiddenExceptionForNonCompliantRole() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("prd-admin")
                .body(Collections.singletonList("someUUID"))
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }
}