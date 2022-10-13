package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;

@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataUpdateIntegrationTest extends AuthorizationEnabledIntegrationTest {


    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    CaseWorkerSkillRepository caseWorkerSkillRepository;

    @Autowired
    StaffAuditRepository staffAuditRepository;

    StaffProfileCreationRequest staffProfileCreationRequest;

    @BeforeEach
    public void setUpClient() {
        super.setUpClient();


        caseWorkerProfileRepository.deleteAll();
        caseWorkerLocationRepository.deleteAll();
        caseWorkerRoleRepository.deleteAll();
        caseWorkerWorkAreaRepository.deleteAll();
        caseWorkerSkillRepository.deleteAll();
        staffAuditRepository.deleteAll();
    }

    @BeforeAll
    public static void setup() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @AfterAll
    public static void tearDown() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }


    @Test
    @Disabled
    public void shouldUpdateCaseWorker() {

        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileCreationRequest();
        userProfilePostUserWireMockForStaffProfile(HttpStatus.CREATED);


        caseworkerReferenceDataClient
                .createStaffProfile(staffProfileCreationRequest, "cwd-admin");


        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(staffProfileCreationRequest, "cwd-admin");
        assertThat(response).containsEntry("http_status", "201 CREATED");

        assertThat(caseWorkerProfileRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerLocationRepository.count()).isEqualTo(1L);
        assertThat(caseWorkerWorkAreaRepository.count()).isEqualTo(1L);
    }

    @Test
    @Disabled
    void should_return_status_code_400_when_invalid_staff_profile() {

        //StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileCreationRequest();

        StaffProfileCreationRequest profile = StaffProfileCreationRequest.staffProfileCreationRequest().build();
        profile.setEmailId("abc.com");
        profile.setFirstName("tester");
        profile.setLastName("tester");

        Map<String, Object> response = caseworkerReferenceDataClient
                .updateStaffProfile(profile, ROLE_STAFF_ADMIN);

        assertThat(response).containsEntry("http_status", "400");

    }

    public StaffProfileCreationRequest getStaffProfileCreationRequest() {
        //Set up Case worker data
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<StaffProfileRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(StaffProfileRoleRequest.staffProfileRoleRequest().role(" role ")
                        .isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(1)
                .location(" location ").build());

        List<CaseWorkerServicesRequest> caseWorkerServicesRequests = ImmutableList.of(CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .service(" areaOfWork ").serviceCode(" serviceCode ")
                .build());

        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .firstName("StaffProfilefirstName ")
                .lastName("StaffProfilelastName ")
                .emailId("test.staffprofilecreate@justice.gov.uk")
                .regionId(1).userType("CTSC")
                .region("region")
                .suspended(false)
                .taskSupervisor(true)
                .caseAllocator(false)
                .staffAdmin(true)
                .roles(caseWorkerRoleRequests)
                .baseLocations(caseWorkerLocationRequests)
                .services(caseWorkerServicesRequests)
                .build();
        return staffProfileCreationRequest;
    }
}