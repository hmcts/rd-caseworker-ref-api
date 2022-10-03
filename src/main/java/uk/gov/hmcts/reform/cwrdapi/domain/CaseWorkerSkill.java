package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


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

    @Column(name = "skill_id", unique = true)
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

}