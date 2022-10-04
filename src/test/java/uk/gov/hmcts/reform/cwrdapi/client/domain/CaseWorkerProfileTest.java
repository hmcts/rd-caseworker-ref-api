package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseWorkerProfileTest {

    @Test
    void testCaseWorkerProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setId("CWID1");
        caseWorkerProfile.setUserId(1L);
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setOfficialEmail("CWtest@test.com");
        caseWorkerProfile.setUserType("User Type");
        caseWorkerProfile.setRegionName("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setSuspended("true");
        caseWorkerProfile.setStaffAdmin("true");
        caseWorkerProfile.setCreatedTime(LocalDateTime.now());
        caseWorkerProfile.setLastUpdatedTime(LocalDateTime.now());
        caseWorkerProfile.setCaseAllocator("caseAllocator");
        caseWorkerProfile.setTaskSupervisor("taskSupervisor");

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getId(), is("CWID1"));
        assertThat(caseWorkerProfile.getUserId(), is(1L));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getOfficialEmail(), is("CWtest@test.com"));
        assertThat(caseWorkerProfile.getUserType(), is("User Type"));
        assertThat(caseWorkerProfile.getRegionName(), is("Region"));
        assertThat(caseWorkerProfile.getRegionId(), is(12));
        assertThat(caseWorkerProfile.getSuspended(), is("true"));
        assertThat(caseWorkerProfile.getStaffAdmin(), is("true"));
        assertNotNull(caseWorkerProfile.getCreatedTime());
        assertNotNull(caseWorkerProfile.getLastUpdatedTime());
        assertFalse(caseWorkerProfile.getCaseAllocator().isEmpty());
        assertFalse(caseWorkerProfile.getTaskSupervisor().isEmpty());
    }

    @Test
    void testCaseWorkerProfileBuilder() {
        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        Skill caseWorkerSkill = new Skill();
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setSkillCode("1");
        caseWorkerSkill.setDescription("Test Skill");

        CaseWorkerProfile caseWorkerProfile = CaseWorkerProfile.builder()
                .id("CWID1")
                .userId(1L)
                .firstName("CWFirstName")
                .lastName("CWLastName")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .idamRoles("idamRole")
                .roles(asList(caseWorkerRole))
                .locations(asList(new Location()))
                .skills(asList(caseWorkerSkill))
                .officialEmail("caseworker@email.com")
                .regionId(1)
                .regionName("regionName")
                .suspended("true")
                .staffAdmin("true")
                .workAreas(asList(new WorkArea()))
                .userType("userType")
                .build();

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getId(), is("CWID1"));
        assertThat(caseWorkerProfile.getUserId(), is(1L));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getOfficialEmail(), is("caseworker@email.com"));
        assertThat(caseWorkerProfile.getUserType(), is("userType"));
        assertThat(caseWorkerProfile.getRegionName(), is("regionName"));
        assertThat(caseWorkerProfile.getRegionId(), is(1));
        assertThat(caseWorkerProfile.getSuspended(), is("true"));
        assertThat(caseWorkerProfile.getStaffAdmin(), is("true"));
        assertNotNull(caseWorkerProfile.getCreatedTime());
        assertNotNull(caseWorkerProfile.getLastUpdatedTime());
        assertNotNull(caseWorkerProfile.getWorkAreas());
        assertNotNull(caseWorkerProfile.getRoles());
        assertNotNull(caseWorkerProfile.getSkills());
        assertNotNull(caseWorkerProfile.getLocations());

        String caseWorkerProfileString = CaseWorkerProfile.builder()
                .id("CWID1").toString();

        assertTrue(caseWorkerProfileString.contains("CaseWorkerProfile.CaseWorkerProfileBuilder(id=CWID1"));
    }

    @Test
    void testCaseWorkerProfileContainingCaseWorkerWorkAreas() {
        WorkArea caseWorkerWorkArea = new WorkArea();
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedTime(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdatedTime(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setWorkAreas(Collections.singletonList(caseWorkerWorkArea));

        assertNotNull(caseWorkerProfile.getWorkAreas());
        assertFalse(caseWorkerProfile.getWorkAreas().isEmpty());
        assertThat(caseWorkerProfile.getWorkAreas().get(0).getAreaOfWork(), is("TestArea"));
        assertThat(caseWorkerProfile.getWorkAreas().get(0).getServiceCode(), is("SvcCode1"));
    }

    @Test
    void testCaseWorkerProfileContainingCaseWorkerRoles() {
        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setRoles(Collections.singletonList(caseWorkerRole));

        assertNotNull(caseWorkerProfile.getRoles());
        assertFalse(caseWorkerProfile.getRoles().isEmpty());
        assertThat(caseWorkerProfile.getRoles().get(0).getRoleId(), is("id"));
        assertThat(caseWorkerProfile.getRoles().get(0).getRoleName(), is("role name"));
    }

    @Test
    void testCaseWorkerProfileContainingCaseWorkerSkills() {
        Skill caseWorkerSkill = new Skill();
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setSkillCode("1");
        caseWorkerSkill.setDescription("Test Skills");

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setSkills(Collections.singletonList(caseWorkerSkill));

        assertNotNull(caseWorkerProfile.getSkills());
        assertFalse(caseWorkerProfile.getSkills().isEmpty());
        assertThat(caseWorkerProfile.getSkills().get(0).getSkillId(), is(1L));
        assertThat(caseWorkerProfile.getSkills().get(0).getSkillCode(), is("1"));
        assertThat(caseWorkerProfile.getSkills().get(0).getDescription(), is("Test Skills"));
    }

    @Test
    void testCaseWorkerProfileContainingCaseWorkerLocations() {
        Location caseWorkerLocation = new Location();
        caseWorkerLocation.setBaseLocationId(1);
        caseWorkerLocation.setLocationName("TestLocation");
        caseWorkerLocation.setPrimary(false);
        caseWorkerLocation.setCreatedTime(LocalDateTime.now());
        caseWorkerLocation.setLastUpdatedTime(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setLocations(Collections.singletonList(caseWorkerLocation));

        assertNotNull(caseWorkerProfile.getLocations());
        assertFalse(caseWorkerProfile.getLocations().isEmpty());
        assertThat(caseWorkerProfile.getLocations().get(0).getBaseLocationId(), is(1));
        assertThat(caseWorkerProfile.getLocations().get(0).getLocationName(), is("TestLocation"));
    }

    @Test
    void testCaseWorkerProfileContainingSuspended() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setSuspended("false");
        assertThat(caseWorkerProfile.getSuspended(), is("false"));
    }

    @Test
    void testCaseWorkerProfileContainingStaffAdmin() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setStaffAdmin("false");
        assertThat(caseWorkerProfile.getStaffAdmin(), is("false"));
    }

}
