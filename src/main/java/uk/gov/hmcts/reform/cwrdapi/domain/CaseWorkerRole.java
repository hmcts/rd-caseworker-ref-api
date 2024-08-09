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


@Entity(name = "case_worker_role")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "case_worker_role_id_seq", sequenceName = "case_worker_role_id_seq", allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "role_id"}))
public class CaseWorkerRole implements Serializable {

    @Id
    @Column(name = "case_worker_role_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_role_id_seq")
    private Long caseWorkerRoleId;

    @Column(name = "case_worker_id")
    private String caseWorkerId;

    @Column(name = "role_id", unique = true)
    private Long roleId;

    @Column(name = "primary_flag")
    private Boolean primaryFlag;

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
    @JoinColumn(name = "role_id", referencedColumnName = "role_id",
            insertable = false, updatable = false, nullable = false)
    private RoleType roleType;

    public CaseWorkerRole(String caseWorkerId, Long roleId, Boolean primaryFlag) {
        this.caseWorkerId = caseWorkerId;
        this.roleId = roleId;
        this.primaryFlag = primaryFlag;
    }

}
