package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerStaticValueRepositoryAccessorImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffProfileCreateUpdateUtilTest {

    @Mock
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;
    @Mock
    CaseWorkerRoleRepository caseWorkerRoleRepository;
    @Mock
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

    private StaffProfileCreationRequest staffProfileCreationRequest;
    private RoleType roleType;
    private UserType userType;
    private Skill skill;
    private CaseWorkerProfile caseWorkerProfile;
    private Set<String> idamRoles = new HashSet<>();
    @InjectMocks
    private StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;

    @BeforeEach
    public void setUp() {
        //MockitoAnnotations.openMocks(this);

        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        CaseWorkerRoleRequest caseWorkerRoleRequest =
                new CaseWorkerRoleRequest("testRole1", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId("1L")
                .description("training")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .roles(singletonList(caseWorkerRoleRequest))
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .staffAdmin(true)
                .services(List.of(caseWorkerServicesRequest,caseWorkerServicesRequest))
                .baseLocations(List.of(caseWorkerLocationRequest,caseWorkerLocationRequest))
                .roles(List.of(caseWorkerRoleRequest,caseWorkerRoleRequest))
                .skills(List.of(skillsRequest,skillsRequest))
                .build();

        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");

        roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("testUser1");

        skill = new Skill();
        skill.setSkillId(1L);
        skill.setDescription("training");
    }

    @Test
    void testPopulateStaffProfile() {
        staffProfileCreateUpdateUtil.populateStaffProfile(
                  staffProfileCreationRequest,new CaseWorkerProfile(),"1");
        verify(caseWorkerStaticValueRepositoryAccessorImpl, times(2)).getRoleTypes();
        verify(caseWorkerStaticValueRepositoryAccessorImpl, times(2)).getSkills();
    }

    @Test
    void testMapCaseWorkerProfileRequest() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");
        CaseWorkerProfile caseWorkerProfile = staffProfileCreateUpdateUtil.mapStaffProfileRequest(
                "1", staffProfileCreationRequest, new CaseWorkerProfile());

        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("1");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo(staffProfileCreationRequest.getFirstName());
        assertThat(caseWorkerProfile.getLastName()).isEqualTo(staffProfileCreationRequest.getLastName());
        assertThat(caseWorkerProfile.getEmailId()).isEqualTo(staffProfileCreationRequest.getEmailId());
        assertThat(caseWorkerProfile.getSuspended()).isFalse();
        assertThat(caseWorkerProfile.getUserTypeId()).isZero();
        assertThat(caseWorkerProfile.getRegionId()).isEqualTo(staffProfileCreationRequest.getRegionId());
        assertThat(caseWorkerProfile.getRegion()).isEqualTo(staffProfileCreationRequest.getRegion());
        assertThat(caseWorkerProfile.getCaseAllocator()).isEqualTo(staffProfileCreationRequest.isCaseAllocator());
        assertThat(caseWorkerProfile.getTaskSupervisor()).isEqualTo(staffProfileCreationRequest.isTaskSupervisor());
    }

    @Test
    void testCaseWorkerRoleRequestMapping() {
        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        List<CaseWorkerRole> caseWorkerRole = staffProfileCreateUpdateUtil.mapStaffRoleRequestMapping(
                "1", staffProfileCreationRequest);

        assertNotNull(caseWorkerRole);
        assertEquals(2,caseWorkerRole.size());
    }

    @Test
    void testCaseWorkerSkillMapping() {
        when(caseWorkerStaticValueRepositoryAccessorImpl.getSkills()).thenReturn(List.of(skill));
        List<CaseWorkerSkill> caseWorkerSkill = staffProfileCreateUpdateUtil.mapStaffSkillRequestMapping(
                "1", staffProfileCreationRequest);
        assertNotNull(caseWorkerSkill);
        assertEquals(2,caseWorkerSkill.size());
    }

    @Test
    void testGetUserRolesByRoleId() {
        staffProfileCreateUpdateUtil.getUserRolesByRoleId(
                staffProfileCreationRequest);
        verify(roleAssocRepository, times(1)).findByRoleTypeInAndServiceCodeIn(any(),any());
        verify(caseWorkerStaticValueRepositoryAccessorImpl, times(2)).getRoleTypes();
    }
}