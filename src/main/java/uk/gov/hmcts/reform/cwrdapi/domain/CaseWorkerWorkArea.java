package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "case_worker_work_area")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "case_worker_work_area_id_seq", sequenceName = "case_worker_work_area_id_seq",
        allocationSize = 1)
public class CaseWorkerWorkArea implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_work_area_id_seq")
    @Column(name = "case_worker_work_area_id")
    private Long caseWorkerWorkAreaId;

    @Column(name = "case_worker_id")
    @NotNull
    @Size(max = 64)
    @NaturalId
    private String caseWorkerId;

    @Column(name = "area_of_work")
    @NotNull
    @Size(max = 128)
    private String areaOfWork;

    @Column(name = "service_code")
    @NotNull
    @Size(max = 16)
    @NaturalId
    private String serviceCode;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne
    @JoinColumn(name = "case_worker_id", referencedColumnName = "case_worker_id",
            insertable = false, updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;
}
