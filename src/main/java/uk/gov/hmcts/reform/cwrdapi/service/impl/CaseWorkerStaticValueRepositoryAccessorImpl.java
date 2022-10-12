package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerStaticValueRepositoryAccessor;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;

@Component
public class CaseWorkerStaticValueRepositoryAccessorImpl implements CaseWorkerStaticValueRepositoryAccessor {
    private List<RoleType> roleTypes;

    private List<UserType> userTypes;

    private List<Skill> skills;

    @Autowired
    private SimpleJpaRepository<RoleType,Long> roleTypeRepository;

    @Autowired
    private SimpleJpaRepository<UserType, Long> userTypeRepository;

    @Autowired
    private SimpleJpaRepository<Skill, Long> skillRepository;

    @PostConstruct
    public void initialize() {
        roleTypes = Collections.unmodifiableList(roleTypeRepository.findAll());
        userTypes = Collections.unmodifiableList(userTypeRepository.findAll());
        skills    = Collections.unmodifiableList(skillRepository.findAll());
    }

    public List<RoleType> getRoleTypes() {
        return roleTypes;
    }

    public List<UserType> getUserTypes() {
        return userTypes;
    }

    public List<Skill> getSkills() {
        return skills;
    }
}
