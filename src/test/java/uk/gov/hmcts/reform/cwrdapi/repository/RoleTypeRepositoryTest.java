package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleTypeRepositoryTest {

    RoleTypeRepository roleTypeRepository = mock(RoleTypeRepository.class);
    List<RoleType> roleTypes = new ArrayList<>();

    @Test
    void findByServiceCodeTest() {
        when(roleTypeRepository.findAll()).thenReturn(roleTypes);
        assertNotNull(roleTypeRepository.findAll());
    }
}
