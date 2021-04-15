package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteCaseWorkerProfilesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Before
    public void setUp() {

        super.setUpClient();

        //Set up Case worker data
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(CaseWorkerRoleRequest.caseWorkerRoleRequest()
                        .role(" role ").isPrimaryFlag(true).build());

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
                .firstName(" firstName ").lastName(" lastName ").emailId("test.inttest@hmcts.gov.uk")
                .regionId(1).userType("CTSC")
                .region("region").suspended(false).roles(caseWorkerRoleRequests).idamRoles(roles)
                .baseLocations(caseWorkerLocationRequests).workerWorkAreaRequests(caseWorkerAreaRequests).build());
    }

    @Test
    public void deleteCaseWorkerProfileById() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        //Create User
        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");

        assertThat(createResponse).containsEntry("http_status", "201 CREATED");

        CaseWorkerProfile createdProfile = caseWorkerProfileRepository.findByEmailId("test.inttest@hmcts.gov.uk");

        //Check Created User Exists
        assertThat(createdProfile).isNotNull();

        userProfileDeleteUserWireMock();

        //Delete User By User ID
        Map<String, Object> deleteResponse = caseworkerReferenceDataClient
                .deleteCaseWorker("/users?userId=" + createdProfile.getCaseWorkerId());

        assertThat(deleteResponse).containsEntry("status", "204 NO_CONTENT");

        Optional<CaseWorkerProfile> deletedProfile =
                caseWorkerProfileRepository.findByCaseWorkerId(createdProfile.getCaseWorkerId());

        //Check Deleted User Does Not Exist
        assertThat(deletedProfile).isEmpty();
    }

    @Test
    public void deleteCaseWorkerProfileByEmailPattern() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        //Create User
        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");

        assertThat(createResponse).containsEntry("http_status", "201 CREATED");

        CaseWorkerProfile createdProfile = caseWorkerProfileRepository.findByEmailId("test.inttest@hmcts.gov.uk");

        //Check Created User Exists
        assertThat(createdProfile).isNotNull();

        userProfileDeleteUserWireMock();

        //Delete User By Email Pattern
        Map<String, Object> deleteResponse = caseworkerReferenceDataClient
                .deleteCaseWorker("/users?emailPattern=" + "test.inttest");

        assertThat(deleteResponse).containsEntry("status", "204 NO_CONTENT");

        CaseWorkerProfile deletedProfile = caseWorkerProfileRepository.findByEmailId("test.inttest@hmcts.gov.uk");

        //Check Deleted User Does Not Exist
        assertThat(deletedProfile).isNull();
    }

    @Test
    public void deleteCaseWorkerProfileByEmailPattern_Returns404WhenNoUsersFoundWithGivenEmailPattern() {
        //Delete User By Email Pattern
        Map<String, Object> deleteResponse = caseworkerReferenceDataClient
                .deleteCaseWorker("/users?emailPattern=" + "INVALID");

        assertThat(deleteResponse).containsEntry("http_status", "404");
    }
}
