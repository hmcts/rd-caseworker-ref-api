package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTypeRepositoryTest {

    UserTypeRepository userTypeRepository = mock(UserTypeRepository.class);

    @Test
    void findAllUserTypesTest() {

        List<UserType> userTypes = getUserTypesData();
        when(userTypeRepository.findAll()).thenReturn(userTypes);
        assertNotNull(userTypeRepository.findAll());

        assertThat(userTypes).hasSize(1);

        UserType userType = userTypes.get(0);

        assertThat(userType.getUserTypeId()).isEqualTo(1L);
        assertThat(userType.getDescription()).isEqualTo("testUser1");
    }

    private List<UserType> getUserTypesData() {

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("testUser1");

        List<UserType> userTypes = List.of(userType);

        return userTypes;
    }


}
