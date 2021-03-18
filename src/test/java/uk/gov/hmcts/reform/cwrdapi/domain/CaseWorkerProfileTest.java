package uk.gov.hmcts.reform.cwrdapi.domain;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerProfileTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testCaseWorkerProfile() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWtest@test.com");
        caseWorkerProfile.setUserTypeId(1L);
        caseWorkerProfile.setRegion("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setSuspended(true);
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());
        caseWorkerProfile.setUserType(userType);

        assertNotNull(caseWorkerProfile);
        assertThat(caseWorkerProfile.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getFirstName(), is("CWFirstName"));
        assertThat(caseWorkerProfile.getLastName(), is("CWLastName"));
        assertThat(caseWorkerProfile.getEmailId(), is("CWtest@test.com"));
        assertThat(caseWorkerProfile.getUserTypeId(), is(1L));
        assertThat(caseWorkerProfile.getRegion(), is("Region"));
        assertThat(caseWorkerProfile.getRegionId(), is(12));
        assertThat(caseWorkerProfile.getSuspended(), is(true));
        assertNotNull(caseWorkerProfile.getCreatedDate());
        assertNotNull(caseWorkerProfile.getLastUpdate());

        assertNotNull(caseWorkerProfile.getUserType());
        assertThat(caseWorkerProfile.getUserType().getUserTypeId(), is(1L));
        assertThat(caseWorkerProfile.getUserType().getDescription(), is("Test Description"));

    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerWorkAreas() {
        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerWorkAreas(Collections.singletonList(caseWorkerWorkArea));

        assertNotNull(caseWorkerProfile.getCaseWorkerWorkAreas());
        assertFalse(caseWorkerProfile.getCaseWorkerWorkAreas().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getCaseWorkerWorkAreaId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getAreaOfWork(), is("TestArea"));
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getServiceCode(), is("SvcCode1"));
    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerRoles() {
        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerRoles(Collections.singletonList(caseWorkerRole));

        assertNotNull(caseWorkerProfile.getCaseWorkerRoles());
        assertFalse(caseWorkerProfile.getCaseWorkerRoles().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getCaseWorkerRoleId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getRoleId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getPrimaryFlag(), is(false));
    }

    @Test
    public void testCaseWorkerProfileContainingCaseWorkerLocations() {
        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerLocationId(1L);
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setLocation("TestLocation");
        caseWorkerLocation.setLocationId(13);
        caseWorkerLocation.setPrimaryFlag(false);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerLocations(Collections.singletonList(caseWorkerLocation));

        assertNotNull(caseWorkerProfile.getCaseWorkerLocations());
        assertFalse(caseWorkerProfile.getCaseWorkerLocations().isEmpty());
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getCaseWorkerLocationId(), is(1L));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocation(), is("TestLocation"));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocationId(), is(13));
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getPrimaryFlag(), is(false));
    }

    @Test
    public void testCaseWorkerProfileContainingSuspended() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setSuspended(false);
        assertFalse(caseWorkerProfile.getSuspended());
    }

    @Test
    public void testCaseWorkerProfileWithNameLongerThan150CharactersIsConstraintViolation() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName(RandomStringUtils.randomAlphabetic(129));
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWtest@test.com");
        caseWorkerProfile.setUserTypeId(1L);
        caseWorkerProfile.setRegion("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setSuspended(true);
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());
        caseWorkerProfile.setUserType(userType);

        Set<ConstraintViolation<CaseWorkerProfile>> violations = validator
                .validate(caseWorkerProfile);

        Assertions.assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    public void testCaseWorkerProfileWithNameIncludingUnallowedSpecialCharactersIsConstraintViolation() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName(RandomStringUtils.randomAlphabetic(15) + "*");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWtest@test.com");
        caseWorkerProfile.setUserTypeId(1L);
        caseWorkerProfile.setRegion("Region");
        caseWorkerProfile.setRegionId(12);
        caseWorkerProfile.setSuspended(true);
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());
        caseWorkerProfile.setUserType(userType);

        Set<ConstraintViolation<CaseWorkerProfile>> violations = validator
                .validate(caseWorkerProfile);

        Assertions.assertThat(violations.size()).isEqualTo(1);
    }
}
