package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserTypeRepositoryTest {

    UserTypeRepository userTypeRepository = mock(UserTypeRepository.class);
    List<UserType> userTypes = new ArrayList<>();

    @Test
    public void findByServiceCodeTest() {
        when(userTypeRepository.findAll()).thenReturn(userTypes);
        assertNotNull(userTypeRepository.findAll());
    }
}
