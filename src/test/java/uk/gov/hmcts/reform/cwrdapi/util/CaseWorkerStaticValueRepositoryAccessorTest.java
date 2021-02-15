package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerStaticValueRepositoryAccessorTest {
    @Mock
    private SimpleJpaRepository<RoleType,Long> roleTypeRepository;

    @Mock
    private SimpleJpaRepository<UserType, Long> userTypeRepository;

    @InjectMocks
    private CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;

    @Test
    public void shouldGetRolesFromRoleTypeRepo() {
        RoleType roleType = new RoleType();
        roleType.setDescription("testRole");
        roleType.setRoleId(1L);
        when(roleTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        caseWorkerStaticValueRepositoryAccessor.initialize();
        assertEquals("testRole",
                caseWorkerStaticValueRepositoryAccessor.getRoleTypes().get(0).getDescription());
    }

    @Test
    public void shouldGetUserTypeFromUserTypeRepo() {
        UserType roleType = new UserType();
        roleType.setDescription("testUser");
        roleType.setUserTypeId(1L);
        when(userTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        caseWorkerStaticValueRepositoryAccessor.initialize();
        assertEquals("testUser",
                caseWorkerStaticValueRepositoryAccessor.getUserTypes().get(0).getDescription());
    }
}