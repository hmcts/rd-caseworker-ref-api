package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaffRefDataUserTypesResponseTest {


    private final StaffRefDataUserTypesResponse staffRefDataUserTypesResponse = new StaffRefDataUserTypesResponse();

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        List<StaffRefDataUserType> refDataUserTypes = new ArrayList<>();
        StaffRefDataUserType staffRefDataUserType = new StaffRefDataUserType();
        staffRefDataUserType.setId(1L);
        staffRefDataUserType.setCode("Test Code");
        refDataUserTypes.add(staffRefDataUserType);
        staffRefDataUserTypesResponse.setUserTypes(refDataUserTypes);
        assertThat(staffRefDataUserTypesResponse.getUserTypes().get(0).getId()).isEqualTo(1L);
        assertThat(staffRefDataUserTypesResponse.getUserTypes().get(0).getCode()).isEqualTo("Test Code");
    }
}