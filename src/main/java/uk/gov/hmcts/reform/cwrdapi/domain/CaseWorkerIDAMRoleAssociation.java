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

@Entity(name = "case_worker_idam_role_assoc")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "cw_idam_role_assoc_id_seq", sequenceName = "cw_idam_role_assoc_id_seq", allocationSize = 1)

public class CaseWorkerIDAMRoleAssociation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cw_idam_role_assoc_id_seq")
    @Column(name = "cw_idam_role_assoc_id")
    private Long caseWorkerIDAMRoleAssociationId;

    @Column(name = "role_id")
    @NotNull
    private Long roleId;

    @Column(name = "service_code")
    @NotNull
    @Size(max = 16)
    private String serviceCode;

    @Column(name = "idam_role")
    @NotNull
    @Size(max = 64)
    private String idamRole;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id",
            insertable = false, updatable = false, nullable = false)
    private RoleType roleType;
}
