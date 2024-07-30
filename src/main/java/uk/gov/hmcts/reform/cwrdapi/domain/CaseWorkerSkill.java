package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity(name = "case_worker_skill")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "case_worker_skill_id_seq", sequenceName = "case_worker_skill_id_seq", allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "skill_id"}))
public class CaseWorkerSkill implements Serializable {

    @Id
    @Column(name = "case_worker_skill_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_skill_id_seq")
    private Long caseWorkerSkillId;

    @Column(name = "case_worker_id")
    private String caseWorkerId;

    @Column(name = "skill_id")
    private Long skillId;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_worker_id", referencedColumnName = "case_worker_id",
            insertable = false, updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", referencedColumnName = "skill_id",
            insertable = false, updatable = false, nullable = false)
    private Skill skill;

    public CaseWorkerSkill(String caseWorkerId, Long skillId) {
        this.caseWorkerId = caseWorkerId;
        this.skillId = skillId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id",insertable = false, updatable = false, nullable = false)
    private CaseWorkerSkill caseWorkerSkill;

}
