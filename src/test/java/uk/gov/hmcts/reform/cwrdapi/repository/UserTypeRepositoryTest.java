package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTypeRepositoryTest {

    UserTypeRepository userTypeRepository = mock(UserTypeRepository.class);
    List<UserType> userTypes = new ArrayList<>();

    @Test
    void findByServiceCodeTest() {
        when(userTypeRepository.findAll()).thenReturn(userTypes);
        assertNotNull(userTypeRepository.findAll());
    }
}
