package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateCaseWorkerProfilesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @BeforeEach
    public void setUpClient() {

        super.setUpClient();

        //Set up Case worker data
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests =
            ImmutableList.of(CaseWorkerRoleRequest.caseWorkerRoleRequest().role(" role ").isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
            .caseWorkersLocationRequest()
            .isPrimaryFlag(true).locationId(1)
            .location(" location ").build());

        List<CaseWorkerWorkAreaRequest> caseWorkerAreaRequests = ImmutableList.of(CaseWorkerWorkAreaRequest
            .caseWorkerWorkAreaRequest()
            .areaOfWork(" areaOfWork ").serviceCode(" serviceCode ")
            .build());

        caseWorkersProfileCreationRequests = ImmutableList.of(CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .firstName(" firstName ")
                .lastName(" lastName ")
                .emailId("test.inttest@hmcts.gov.uk")
                .regionId(1).userType("CTSC")
                .region("region")
                .suspended(false)
                .taskSupervisor(true)
                .caseAllocator(false)
                .roles(caseWorkerRoleRequests)
                .idamRoles(roles)
                .baseLocations(caseWorkerLocationRequests)
                .workerWorkAreaRequests(caseWorkerAreaRequests)
                .build());
        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();

    }

    @Test
    public void shouldCreateCaseWorker() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");
        assertThat(response).containsEntry("http_status", "201 CREATED");

        assertThat(caseWorkerProfileRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerLocationRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerWorkAreaRepository.count()).isEqualTo(1L);
        Set<String> emails = new HashSet<>();
        emails.add("test.inttest@hmcts.gov.uk");
        CaseWorkerProfile profile =
                caseWorkerProfileRepository.findByEmailIdIn(emails).get(0);
        assertThat(profile.getFirstName()).isEqualTo("firstName");
        assertThat(profile.getLastName()).isEqualTo("lastName");
        assertThat(profile.getEmailId()).isEqualTo("test.inttest@hmcts.gov.uk");
        assertThat(profile.getRegion()).isEqualTo("region");
        assertFalse(profile.getCaseAllocator());
        assertTrue(profile.getTaskSupervisor());

        List<CaseWorkerLocation> caseWorkerLocations = caseWorkerLocationRepository.findAll();
        CaseWorkerLocation caseWorkerLocation = caseWorkerLocations.get(0);
        assertThat(caseWorkerLocation.getLocation()).isEqualTo("location");

        List<CaseWorkerWorkArea> caseWorkerWorkAreas = caseWorkerWorkAreaRepository.findAll();
        CaseWorkerWorkArea caseWorkerWorkArea = caseWorkerWorkAreas.get(0);
        assertThat(caseWorkerWorkArea.getAreaOfWork()).isEqualTo("areaOfWork");
        assertThat(caseWorkerWorkArea.getServiceCode()).isEqualTo("serviceCode");
    }

    @Test
    public void shouldRollbackCreateCaseWorkerInCaseOfException() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Mockito.doThrow(new RuntimeException("jms exception"))
                .when(topicPublisher).sendMessage(Mockito.any());
        assertThat(caseWorkerProfileRepository.count()).isZero();

        Map<String, Object> response = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");
        assertThat(response).containsEntry("http_status", "500");

        assertThat(caseWorkerProfileRepository.count()).isZero();
        assertThat(caseWorkerLocationRepository.count()).isZero();
        assertThat(caseWorkerRoleRepository.count()).isZero();
        assertThat(caseWorkerWorkAreaRepository.count()).isZero();
    }

    @Test
    void shouldCreateCaseworkerWithNewRoles() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        CaseWorkerRoleRequest cwRoleRequest = new CaseWorkerRoleRequest("Regional Centre Administrator", true);
        CaseWorkerRoleRequest cwRoleRequest1 = new CaseWorkerRoleRequest("Regional Centre Team Leader", false);
        CaseWorkerRoleRequest cwRoleRequest2 = new CaseWorkerRoleRequest("DWP Administrator", false);

        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = ImmutableList
            .of(cwRoleRequest,cwRoleRequest1,cwRoleRequest2);
        caseWorkersProfileCreationRequests.get(0).setRoles(caseWorkerRoleRequests);
        caseWorkersProfileCreationRequests.get(0).setUserType("Other Government Department");

        Map<String, Object> response = caseworkerReferenceDataClient
            .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");
        assertThat(response).containsEntry("http_status", "201 CREATED");
        List<CaseWorkerRole> caseWorkerRoles = caseWorkerRoleRepository.findAll();
        var caseWorkerProfile = caseWorkerProfileRepository.findAll();
        assertEquals(13, (long) caseWorkerRoles.get(0).getRoleId());
        assertEquals(14,(long)caseWorkerRoles.get(2).getRoleId());
        assertEquals(5,caseWorkerProfile.get(0).getUserTypeId());



    }


}
