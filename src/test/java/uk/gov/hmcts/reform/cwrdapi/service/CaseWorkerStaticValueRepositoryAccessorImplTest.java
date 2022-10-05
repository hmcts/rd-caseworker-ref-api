package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerStaticValueRepositoryAccessorImpl;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerStaticValueRepositoryAccessorImplTest {
    @Mock
    private SimpleJpaRepository<RoleType,Long> roleTypeRepository;

    @Mock
    private SimpleJpaRepository<UserType, Long> userTypeRepository;

    @Mock
    private SimpleJpaRepository<Skill, Long> skillRepository;

    @InjectMocks
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;

    @Test
    void shouldGetRolesFromRoleTypeRepo() {
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
    void shouldGetUserTypeFromUserTypeRepo() {
        UserType roleType = new UserType();
        roleType.setDescription("testUser");
        roleType.setUserTypeId(1L);
        when(userTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        caseWorkerStaticValueRepositoryAccessorImpl.initialize();
        assertEquals("testUser",
                caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes().get(0).getDescription());
        verify(userTypeRepository, times(1)).findAll();
    }

    @Test
    void shouldGetSkillFromSkillRepo() {
        Skill skill = new Skill();
        skill.setDescription("testSkill");
        skill.setSkillId(1L);
        when(skillRepository.findAll()).thenReturn(Collections.singletonList(skill));
        caseWorkerStaticValueRepositoryAccessorImpl.initialize();
        assertEquals("testSkill",
                caseWorkerStaticValueRepositoryAccessorImpl.getSkills().get(0).getDescription());
        verify(skillRepository, times(1)).findAll();
    }
}