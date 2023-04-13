package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleTypeRepositoryTest {

    RoleTypeRepository roleTypeRepository = mock(RoleTypeRepository.class);

    @Test
    void findAllRoleTypesTest() {
        List<RoleType> roleTypes = getUserTypesData();
        when(roleTypeRepository.findAll()).thenReturn(roleTypes);
        assertNotNull(roleTypeRepository.findAll());

        assertThat(roleTypes).hasSize(1);

        RoleType roleType = roleTypes.get(0);

        assertThat(roleType.getRoleId()).isEqualTo(1L);
        assertThat(roleType.getDescription()).isEqualTo("testRole1");

    }

    private List<RoleType> getUserTypesData() {

        RoleType roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");


        List<RoleType> roleTypes = List.of(roleType);

        return roleTypes;
    }
}
