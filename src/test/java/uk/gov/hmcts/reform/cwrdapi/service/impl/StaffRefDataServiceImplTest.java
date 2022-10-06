package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPagination;


@SuppressWarnings("AbbreviationAsWordInName")
@ExtendWith(MockitoExtension.class)
class StaffRefDataServiceImplTest {
    @Mock
    private SkillRepository skillRepository;

    @Mock
    CaseWorkerProfileRepository caseWorkerProfileRepo;


    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void should_return_non_empty_list_of_service_skills() {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("BBA3");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(1L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("A1");
        assertThat(skillDTO.getDescription()).isEqualTo("desc1");
        assertThat(skillDTO.getUserType()).isEqualTo("user_type1");

    }

    @Test
    void should_return_empty_list_of_service_skills() {
        List<Skill> skills = new ArrayList<>();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();

        assertThat(serviceSkills).isEmpty();
    }

    @Test
    void should_return_null_list_of_service_skills() {
        List<Skill> skills = null;
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills).isNotNull();

        assertThat(serviceSkills).isEmpty();
    }

    @Test
    void should_return_case_worker_profile_with_status_code_200() {

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);

        String searchString = "cwr";
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();
        caseWorkerProfiles.add(caseWorkerProfile);
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        when(caseWorkerProfileRepo.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
        validateSearchStaffUserResponses(searchResponse);
    }

    @Test
    void should_return_case_worker_profile_with_status_code_200_when_missing_boolean_values() {

        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();

        caseWorkerProfile.setSuspended(null);
        caseWorkerProfile.setTaskSupervisor(null);
        caseWorkerProfile.setCaseAllocator(null);
        caseWorkerProfile.setUserAdmin(null);

        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);
        String searchString = "cwr";
        when(caseWorkerProfileRepo.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
    }

    @Test
    void should_return_empty_list_of_case_worker_profile_with_status_code_200() {

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);

        String searchString = "cwr";
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        when(caseWorkerProfileRepo.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(searchResponse).isEmpty();
    }


    void validateSearchStaffUserResponses(List<SearchStaffUserResponse> searchResponses) {
        assertThat(searchResponses).hasSize(1);

        SearchStaffUserResponse searchStaffUserResponse = searchResponses.get(0);
        assertThat(searchStaffUserResponse.getFirstName()).isEqualTo("firstName");
        assertThat(searchStaffUserResponse.getLastName()).isEqualTo("Last`name");
        assertThat(searchStaffUserResponse.getEmailId()).isEqualTo("a@b.com");
        assertThat(searchStaffUserResponse.isSuspended()).isTrue();
        assertThat(searchStaffUserResponse.isTaskSupervisor()).isTrue();
        assertThat(searchStaffUserResponse.isCaseAllocator()).isFalse();
        assertThat(searchStaffUserResponse.isStaffAdmin()).isTrue();

        ServiceResponse serviceResponse = searchStaffUserResponse.getServices().get(0);

        assertThat(serviceResponse.getService()).isEqualTo("TestArea");
        assertThat(serviceResponse.getServiceCode()).isEqualTo("SvcCode1");

        assertThat(searchStaffUserResponse.getRegionId()).isEqualTo(111122222);
        assertThat(searchStaffUserResponse.getRegion()).isEqualTo("region");

        Role role = searchStaffUserResponse.getRoles().get(0);

        assertThat(role.getRoleId()).isEqualTo("1");
        assertThat(role.getRoleName()).isEqualTo("testRole1");

        Location location = searchStaffUserResponse.getBaseLocations().get(0);

        assertThat(location.getBaseLocationId()).isEqualTo(11112);
        assertThat(location.getLocationName()).isEqualTo("test location");
        assertThat(searchStaffUserResponse.getUserType()).isEqualTo("userTypeId");

        SkillResponse skillResponse = searchStaffUserResponse.getSkills().get(0);

        assertThat(skillResponse.getSkillId()).isEqualTo(1L);
        assertThat(skillResponse.getDescription()).isEqualTo("desc1");


    }

    CaseWorkerProfile buildCaseWorkerProfile() {

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());

        RoleType roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        caseWorkerRole.setRoleType(roleType);

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setCaseWorkerLocationId(11111L);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());
        caseWorkerLocation.setLocationId(11112);
        caseWorkerLocation.setLocation("test location");
        caseWorkerLocation.setPrimaryFlag(true);

        UserType userType = new UserType();
        userType.setDescription("userTypeId");

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setFirstName("firstName");
        caseWorkerProfile.setLastName("Last`name");
        caseWorkerProfile.setEmailId("a@b.com");
        caseWorkerProfile.setRegion("region");
        caseWorkerProfile.setRegionId(111122222);
        caseWorkerProfile.setUserTypeId(112L);
        caseWorkerProfile.setUserType(userType);
        caseWorkerProfile.setSuspended(true);
        caseWorkerProfile.setTaskSupervisor(true);
        caseWorkerProfile.setCaseAllocator(false);
        caseWorkerProfile.setUserAdmin(true);
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerId("27fbd198-552e-4c32-9caf-37be1545caaf");
        caseWorkerProfile.setCaseWorkerRoles(singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerLocations(singletonList(caseWorkerLocation));
        CaseWorkerSkill caseWorkerSkill = getCaseWorkerSkill();

        caseWorkerProfile.setCaseWorkerSkills(singletonList(caseWorkerSkill));

        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerWorkAreas(singletonList(caseWorkerWorkArea));
        return caseWorkerProfile;
    }

    uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile buildCaseWorkerProfileForDto() {
        Role
                role = Role.builder()
                .roleId("1")
                .roleName("roleName")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .isPrimary(true)
                .build();
        Location location = Location
                .builder()
                .baseLocationId(11111)
                .locationName("LocationName")
                .isPrimary(true)
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();
        WorkArea workArea = WorkArea.builder()
                .areaOfWork("areaOfWork")
                .serviceCode("serviceCode")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();

        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile.builder()
                        .id("27fbd198-552e-4c32-9caf-37be1545caaf")
                        .firstName("firstName")
                        .lastName("lastName")
                        .officialEmail("a@b.com")
                        .regionName("regionName")
                        .regionId(1)
                        .userType("userType")
                        .userId(11111L)
                        .suspended("false")
                        .createdTime(LocalDateTime.now())
                        .lastUpdatedTime(LocalDateTime.now())
                        .roles(singletonList(role))
                        .locations(singletonList(location))
                        .workAreas(singletonList(workArea))
                        .taskSupervisor("Y")
                        .caseAllocator("N")
                        .build();

        return caseWorkerProfile;
    }

    private CaseWorkerSkill getCaseWorkerSkill() {

        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setCaseWorkerId("423ec46f-359f-4fcc-9ecc-b1cab4d7e683");
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setSkills(getSkillsData());

        return caseWorkerSkill;

    }





    private List<Skill> getSkillsData() {
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1L);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        Skill skill2 = new Skill();
        skill2.setServiceId("BBA3");

        skill2.setSkillId(1L);

        skill2.setSkillId(3L);

        skill2.setSkillCode("A3");
        skill2.setDescription("desc3");
        skill2.setUserType("user_type3");


        Skill skill3 = new Skill();
        skill3.setServiceId("ABA1");
        skill3.setSkillId(2L);
        skill3.setSkillCode("A2");
        skill3.setDescription("desc2");
        skill3.setUserType("user_type2");

        Skill skill4 = new Skill();
        skill4.setServiceId("ABA1");
        skill4.setSkillId(4L);
        skill4.setSkillCode("A4");
        skill4.setDescription("desc4");
        skill4.setUserType("user_type4");

        List<Skill> skills = List.of(skill1, skill2, skill3, skill4);

        return skills;
    }
}
