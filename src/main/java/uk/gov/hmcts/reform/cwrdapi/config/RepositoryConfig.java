package uk.gov.hmcts.reform.cwrdapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Configuration
public class RepositoryConfig {

    @Autowired
    EntityManager entityManager;

    @Bean
    @Transactional
    public SimpleJpaRepository<CaseWorkerAudit, Long> getCaseWorkerAuditRepository() {
        return new SimpleJpaRepository<>(CaseWorkerAudit.class, entityManager);
    }

    @Bean
    @Transactional
    public SimpleJpaRepository<ExceptionCaseWorker, Long> getExceptionCaseWorkerRepository() {
        return new SimpleJpaRepository<>(ExceptionCaseWorker.class, entityManager);
    }

    @Bean
    public SimpleJpaRepository<RoleType, Long> getRoleTypeRepository() {
        return new SimpleJpaRepository<>(RoleType.class, entityManager);
    }

    @Bean
    public SimpleJpaRepository<UserType, Long> getUserTypeRepository() {
        return new SimpleJpaRepository<>(UserType.class, entityManager);
    }

    @Bean
    public SimpleJpaRepository<Skill, Long> getskillRepository() {
        return new SimpleJpaRepository<>(Skill.class, entityManager);
    }

    @Bean
    public SimpleJpaRepository<CaseWorkerSkill, Long> getCaseWorkerSkillAuditRepository() {
        return new SimpleJpaRepository<>(CaseWorkerSkill.class, entityManager);
    }

    @Bean
    public SimpleJpaRepository<StaffAudit, Long> getStaffAuditRepository() {
        return new SimpleJpaRepository<>(StaffAudit.class, entityManager);
    }
}
