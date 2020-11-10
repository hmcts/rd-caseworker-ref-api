package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity(name = "case_worker_location")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "location_id"}))
public class CaseWorkerLocation implements Serializable {

    @Id
    @Column(name = "case_worker_location_id")
    private Long caseWorkerLocationId;

    @Column(name = "case_worker_id")
    @NotNull
    @Size(max = 64)
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

    @ManyToOne
    @JoinColumn(name = "case_worker_id", referencedColumnName = "case_worker_id",
            insertable = false, updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;

}
