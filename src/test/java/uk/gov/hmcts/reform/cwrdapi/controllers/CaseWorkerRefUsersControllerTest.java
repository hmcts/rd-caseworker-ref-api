package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileUpdateservice;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerRefUsersControllerTest {

    CaseWorkerService caseWorkerServiceMock;
    CaseWorkerProfileUpdateservice caseWorkerProfileUpdateserviceMock;
    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest = new ArrayList<>();
    CaseWorkersProfileCreationRequest cwRequest;
    CaseWorkerProfileCreationResponse cwProfileCreationResponse;
    CaseWorkerProfileCreationResponse cwResponse;
    ResponseEntity<Object> responseEntity;
    @InjectMocks
    private CaseWorkerRefUsersController caseWorkerRefUsersController;

    @BeforeEach
    void setUp() {
        caseWorkerServiceMock = mock(CaseWorkerService.class);
        caseWorkerProfileUpdateserviceMock = mock(CaseWorkerProfileUpdateservice.class);
        cwResponse = CaseWorkerProfileCreationResponse
                .builder()
                .caseWorkerRegistrationResponse("Case Worker Profiles Created.")
                .caseWorkerIds(Collections.emptyList())
                .build();

        responseEntity = new ResponseEntity<>(cwResponse, null, HttpStatus.OK);

        Set<String> roles = new HashSet<>();
        roles.add("tribunal_case_worker");

        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = new ArrayList<>();
        CaseWorkerRoleRequest cwRoleRequest = new CaseWorkerRoleRequest("role", true);
        caseWorkerRoleRequests.add(cwRoleRequest);

        List<CaseWorkerLocationRequest> caseWorkeLocationRequests = new ArrayList<>();

        CaseWorkerLocationRequest cwLocRequest = new CaseWorkerLocationRequest(1, "location",
                true);
        caseWorkeLocationRequests.add(cwLocRequest);

        List<CaseWorkerWorkAreaRequest> caseWorkeAreaRequests = new ArrayList<>();
        CaseWorkerWorkAreaRequest cwAreaRequest = new CaseWorkerWorkAreaRequest("areaOfwork",
                "serviceCode");
        caseWorkeAreaRequests.add(cwAreaRequest);

        cwRequest = CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .firstName("firstName")
                .lastName("lastName")
                .emailId("test@gmail.com")
                .regionId(1)
                .userType("userType")
                .region("region")
                .suspended(false)
                .idamRoles(roles)
                .roles(caseWorkerRoleRequests)
                .workerWorkAreaRequests(caseWorkeAreaRequests)
                .baseLocations(caseWorkeLocationRequests)
                .rowId(0)
                .caseAllocator(false)
                .taskSupervisor(false)
                .build();

        caseWorkersProfileCreationRequest.add(cwRequest);

        cwProfileCreationResponse = CaseWorkerProfileCreationResponse
                .builder()
                .caseWorkerRegistrationResponse("")
                .caseWorkerIds(Collections.emptyList())
                .build();
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void createCaseWorkerProfilesTest() {
        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> actual =
                caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);

    }

    @Test
    void test_sendCwDataToTopic_called_when_ids_exists() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("1234");

        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
                .thenReturn(Collections.singletonList(caseWorkerProfile));

        ResponseEntity<?> actual =
                caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);
        verify(caseWorkerServiceMock, times(1)).publishCaseWorkerDataToTopic(any());
    }

    @Test
    void test_sendCwDataToTopic_not_called_when_no_ids_exists() {
        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> actual =
                caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);
        verify(caseWorkerServiceMock, times(0))
                .publishCaseWorkerDataToTopic(any());
    }

    @Test
    void createCaseWorkerProfilesShouldThrow400() {
        caseWorkersProfileCreationRequest = null;
        Assertions.assertThrows(InvalidRequestException.class, () ->
            caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest));

    }

    @Test
    void fetchCaseworkersByIdShouldThrow400() {
        final UserRequest userRequest = UserRequest.builder().userIds(Collections.emptyList()).build();
        Assertions.assertThrows(InvalidRequestException.class, () ->
            caseWorkerRefUsersController.fetchCaseworkersById(userRequest));
    }

    @Test
    void shouldFetchCaseworkerDetails() {
        responseEntity = ResponseEntity.ok().body(null);
        when(caseWorkerServiceMock.fetchCaseworkersById(any()))
                .thenReturn(responseEntity);
        UserRequest userRequest = UserRequest.builder().userIds(Arrays.asList(
                "185a0254-ff80-458b-8f62-2a759788afd2", "2dee918c-279d-40a0-a4c2-871758d78cf0"))
                .build();
        ResponseEntity<?> actual = caseWorkerRefUsersController.fetchCaseworkersById(userRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .fetchCaseworkersById(Arrays.asList(
                        "185a0254-ff80-458b-8f62-2a759788afd2", "2dee918c-279d-40a0-a4c2-871758d78cf0"));
    }

    @Test
    void createCaseWorkerProfileWithNewRole() {
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = new ArrayList<>();
        CaseWorkerRoleRequest cwRoleRequest = new CaseWorkerRoleRequest("Regional Centre Administrator", true);
        CaseWorkerRoleRequest cwRoleRequest1 = new CaseWorkerRoleRequest("Regional Centre Team Leader", false);
        CaseWorkerRoleRequest cwRoleRequest2 = new CaseWorkerRoleRequest("DWP Caseworker",false);
        CaseWorkerRoleRequest cwRoleRequest3 = new CaseWorkerRoleRequest("Registrar",false);
        CaseWorkerRoleRequest cwRoleRequest4 = new CaseWorkerRoleRequest("IBCA Caseworker",false);
        CaseWorkerRoleRequest cwRoleRequest5 = new CaseWorkerRoleRequest("WLU Administrator",false);
        CaseWorkerRoleRequest cwRoleRequest6 = new CaseWorkerRoleRequest("WLU Team Leader",false);
        caseWorkerRoleRequests.add(cwRoleRequest);
        caseWorkerRoleRequests.add(cwRoleRequest1);
        caseWorkerRoleRequests.add(cwRoleRequest2);
        caseWorkerRoleRequests.add(cwRoleRequest3);
        caseWorkerRoleRequests.add(cwRoleRequest4);
        caseWorkerRoleRequests.add(cwRoleRequest5);
        caseWorkerRoleRequests.add(cwRoleRequest6);
        caseWorkersProfileCreationRequest.get(0).setRoles(caseWorkerRoleRequests);
        caseWorkersProfileCreationRequest.get(0).setUserType("Other Government Department");
        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> actual =
                caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);
    }

    @Test
    void updateCaseWorkerProfileFromProfileSync() {

        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName("first_name")
            .lastName("last_name")
            .userId("185a0254-ff80-458b-8f62-2a759788afd2")
            .emailId("firstname@justice.gov.uk")
            .build();
        when(caseWorkerProfileUpdateserviceMock.updateCaseWorkerProfile(caseWorkersProfileUpdationRequest))
            .thenReturn(any());
        ResponseEntity<?> actual =
            caseWorkerRefUsersController.updateCaseWorkerDetails(caseWorkersProfileUpdationRequest);

        assertNotNull(actual);
        verify(caseWorkerProfileUpdateserviceMock, times(1))
            .updateCaseWorkerProfile(caseWorkersProfileUpdationRequest);

    }

    @Test
    void updateCaseWorkerProfileFromProfileSyncShouldThrow400() {
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = null;
        Assertions.assertThrows(InvalidRequestException.class, () ->
            caseWorkerRefUsersController.updateCaseWorkerDetails(caseWorkersProfileUpdationRequest));

    }
}
