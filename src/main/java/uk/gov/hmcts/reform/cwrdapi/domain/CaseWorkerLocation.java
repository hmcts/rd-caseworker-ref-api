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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "case_worker_location")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "case_worker_location_id_seq",
        sequenceName = "case_worker_location_id_seq", allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "location_id"}))
public class CaseWorkerLocation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_location_id_seq")
    @Column(name = "case_worker_location_id")
    private Long caseWorkerLocationId;

    @Column(name = "case_worker_id")
    private String caseWorkerId;

    @Column(name = "location")
    @NotNull
    @Size(max = 128)
    private String location;

    @Column(name = "location_id")
    private Integer locationId;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "primary_flag")
    private Boolean primaryFlag;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_worker_id",referencedColumnName = "case_worker_id",insertable = false,
            updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;

    public CaseWorkerLocation(String caseWorkerId, Integer locationId,
                              String location, Boolean primaryFlag) {

        this.caseWorkerId = caseWorkerId;
        this.locationId = locationId;
        this.location = location;
        this.primaryFlag = primaryFlag;
    }

}
