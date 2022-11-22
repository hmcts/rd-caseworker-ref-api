package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StaffWorkerSkillResponseTest {
    @Test
    void testSkill() {
        StaffWorkerSkillResponse staffWorkerSkillResponse = new StaffWorkerSkillResponse();
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);

        assertNotNull(staffWorkerSkillResponse.getServiceSkills());
    }
}
