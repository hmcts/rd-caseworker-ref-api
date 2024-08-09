package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;



@Entity(name = "skill")
@Getter
@Setter
@NoArgsConstructor

public class Skill implements Serializable {

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "skill_code")
    @Size(max = 62)
    private String skillCode;

    @Column(name = "service_id")
    @Size(max = 64)
    private String serviceId;

    @Column(name = "user_type")
    @Size(max = 512)
    private String userType;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    public Skill(Long skillId, String description) {
        this.skillId = skillId;
        this.description = description;
    }



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id",insertable = false, updatable = false, nullable = false)
    private CaseWorkerSkill caseWorkerSkill;


}
