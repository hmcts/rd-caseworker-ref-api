package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileUpdatedDataTest {

    @Test
    public void test_should_add_roles_add_when_modified() {

        UserProfileUpdatedData userProfileUpdatedData =  UserProfileUpdatedData.builder().idamStatus("ACTIVE").build();
        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");
    }
}