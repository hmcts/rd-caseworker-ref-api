package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class StaffRefDataJobTitleResponseTest {

    private final StaffRefJobTitleResponse staffRefJobTitleResponse = new StaffRefJobTitleResponse();

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        List<StaffRefDataJobTitle> refDataJobTitles = new ArrayList<>();
        StaffRefDataJobTitle staffRefDataJobTitle = new StaffRefDataJobTitle();
        staffRefDataJobTitle.setRoleId(1L);
        staffRefDataJobTitle.setRoleDescription("Test Code");
        refDataJobTitles.add(staffRefDataJobTitle);
        staffRefJobTitleResponse.setJobTitles(refDataJobTitles);
        assertThat(staffRefJobTitleResponse.getJobTitles().get(0).getRoleId()).isEqualTo(1L);
        assertThat(staffRefJobTitleResponse.getJobTitles().get(0).getRoleDescription()).isEqualTo("Test Code");
    }

}
