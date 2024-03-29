package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffProfileWithServiceNameTest {

    @Test
    void testStaffProfileWithServiceName() {
        StaffProfileWithServiceName staffProfileWithServiceName = new StaffProfileWithServiceName();
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        staffProfileWithServiceName.setCcdServiceName("cmc");
        staffProfileWithServiceName.setStaffProfile(caseWorkerProfile);

        assertNotNull(staffProfileWithServiceName);
        assertEquals(caseWorkerProfile, staffProfileWithServiceName.getStaffProfile());
        assertEquals("cmc", staffProfileWithServiceName.getCcdServiceName());
    }

    @Test
    void testStaffProfileWithServiceNameBuilder() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        StaffProfileWithServiceName staffProfileWithServiceName = StaffProfileWithServiceName.builder()
                .ccdServiceName("cmc")
                .staffProfile(caseWorkerProfile)
                .build();

        assertNotNull(staffProfileWithServiceName);
        assertEquals("cmc", staffProfileWithServiceName.getCcdServiceName());
        assertEquals(caseWorkerProfile, staffProfileWithServiceName.getStaffProfile());

        String staffProfileWithServiceNameString = StaffProfileWithServiceName.builder()
                .ccdServiceName("cmc").toString();
        assertTrue(staffProfileWithServiceNameString
                .contains("StaffProfileWithServiceName.StaffProfileWithServiceNameBuilder(ccdServiceName=cmc"));

    }

}
