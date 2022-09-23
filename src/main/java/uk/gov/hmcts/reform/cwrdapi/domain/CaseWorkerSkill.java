package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import static javax.persistence.CascadeType.ALL;

@Entity(name = "case_worker_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerSkill {
    @Id
    @Column(name = "case_worker_skill_id")
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

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = Skill.class, mappedBy = "caseWorkerSkill", cascade = ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

}
