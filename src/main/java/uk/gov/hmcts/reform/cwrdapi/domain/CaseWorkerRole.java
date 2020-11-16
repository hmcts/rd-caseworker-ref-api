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


@Entity(name = "case_worker_role")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "case_worker_role_id_seq", sequenceName = "case_worker_role_id_seq", allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"case_worker_id", "role_id"}))
public class CaseWorkerRole implements Serializable {

    @Id
    @Column(name = "case_worker_role_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_worker_role_id_seq")
    private Long caseWorkerRoleId;

    @Column(name = "case_worker_id")
    @NotNull
    @Size(max = 64)
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

    @ManyToOne
    @JoinColumn(name = "case_worker_id", referencedColumnName = "case_worker_id",
            insertable = false, updatable = false, nullable = false)
    private CaseWorkerProfile caseWorkerProfile;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id",
            insertable = false, updatable = false, nullable = false)
    private RoleType roleType;

}
