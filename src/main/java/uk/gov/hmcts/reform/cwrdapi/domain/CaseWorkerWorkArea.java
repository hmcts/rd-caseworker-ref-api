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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity(name = "case_worker_work_area")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "case_worker_work_area_id_seq", sequenceName = "case_worker_work_area_id_seq",
    allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "service_code"}))
public class CaseWorkerWorkArea implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_work_area_id_seq")
    @Column(name = "case_worker_work_area_id")
    private Long caseWorkerWorkAreaId;

    @Column(name = "case_worker_id")
    private String caseWorkerId;

    @Column(name = "area_of_work")
    @NotNull
    @Size(max = 128)
    private String areaOfWork;

    @Column(name = "service_code")
    @NotNull
    @Size(max = 16)
    private String serviceCode;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_worker_id", referencedColumnName = "case_worker_id",
        insertable = false, updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;

    public CaseWorkerWorkArea(String caseWorkerId, String areaOfWork, String serviceCode) {
        this.caseWorkerId = caseWorkerId;
        this.areaOfWork = areaOfWork;
        this.serviceCode = serviceCode;
    }
}
