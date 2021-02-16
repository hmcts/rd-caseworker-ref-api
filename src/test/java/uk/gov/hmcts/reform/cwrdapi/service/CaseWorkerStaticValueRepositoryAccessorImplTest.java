package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerStaticValueRepositoryAccessorImpl;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerStaticValueRepositoryAccessorImplTest {
    @Mock
    private SimpleJpaRepository<RoleType,Long> roleTypeRepository;

    @Mock
    private SimpleJpaRepository<UserType, Long> userTypeRepository;

    @InjectMocks
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;

    @Test
    public void shouldGetRolesFromRoleTypeRepo() {
        RoleType roleType = new RoleType();
        roleType.setDescription("testRole");
        roleType.setRoleId(1L);
        when(roleTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        caseWorkerStaticValueRepositoryAccessorImpl.initialize();
        assertEquals("testRole",
                caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes().get(0).getDescription());
        verify(roleTypeRepository, times(1)).findAll();
    }

    @Test
    public void shouldGetUserTypeFromUserTypeRepo() {
        UserType roleType = new UserType();
        roleType.setDescription("testUser");
        roleType.setUserTypeId(1L);
        when(userTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        caseWorkerStaticValueRepositoryAccessorImpl.initialize();
        assertEquals("testUser",
                caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes().get(0).getDescription());
        verify(userTypeRepository, times(1)).findAll();
    }
}