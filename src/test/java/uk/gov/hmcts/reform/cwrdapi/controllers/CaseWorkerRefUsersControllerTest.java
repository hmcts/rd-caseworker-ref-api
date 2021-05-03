package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.RequestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerRefUsersControllerTest {

    @InjectMocks
    private CaseWorkerRefUsersController caseWorkerRefUsersController;

    CaseWorkerService caseWorkerServiceMock;

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest = new ArrayList<>();
    CaseWorkersProfileCreationRequest cwRequest;
    CaseWorkerProfileCreationResponse cwProfileCreationResponse;
    CaseWorkerProfileCreationResponse cwResponse;
    ResponseEntity<Object> responseEntity;

    @Before
    public void setUp() {
        caseWorkerServiceMock = mock(CaseWorkerService.class);

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

        cwRequest = new CaseWorkersProfileCreationRequest("firstName",
                "lastName", "test@gmail.com", 1, "userType",
                "region", false, roles, caseWorkerRoleRequests,
                caseWorkeLocationRequests, caseWorkeAreaRequests, 0);

        caseWorkersProfileCreationRequest.add(cwRequest);

        cwProfileCreationResponse = CaseWorkerProfileCreationResponse
                .builder()
                .caseWorkerRegistrationResponse("")
                .caseWorkerIds(Collections.emptyList())
                .build();
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void createCaseWorkerProfilesTest() {
        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
                .thenReturn(Collections.emptyList());
        ResponseEntity<?> actual =
                caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);

    }

    @Test
    public void test_sendCwDataToTopic_called_when_ids_exists() {
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
    public void test_sendCwDataToTopic_not_called_when_no_ids_exists() {
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

    @Test(expected = InvalidRequestException.class)
    public void createCaseWorkerProfilesShouldThrow400() {
        caseWorkersProfileCreationRequest = null;
        caseWorkerRefUsersController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);
    }

    @Test(expected = InvalidRequestException.class)
    public void fetchCaseworkersByIdShouldThrow400() {
        caseWorkerRefUsersController.fetchCaseworkersById(
                UserRequest.builder().userIds(Collections.emptyList()).build());
    }

    @Test
    public void shouldFetchCaseworkerDetails() {
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
    public void shouldFetchStaffByCcdServiceNames() {
        responseEntity = ResponseEntity.ok().body(null);
        when(caseWorkerServiceMock.fetchStaffProfilesForRoleRefresh(any(), any()))
                .thenReturn(responseEntity);

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC", "test",
                20, "id");

        ResponseEntity<?> actual = caseWorkerRefUsersController
                .fetchStaffByCcdServiceNames("cmc", 1, 0,
                        "ASC", "caseWorkerId");

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .fetchStaffProfilesForRoleRefresh("cmc", pageRequest);
    }

    @Test(expected = InvalidRequestException.class)
    public void shouldThrowInvalidRequestExceptionForEmptyServiceName() {
        caseWorkerRefUsersController
                .fetchStaffByCcdServiceNames("", 1, 0,
                        "ASC", "caseWorkerId");
    }
}
