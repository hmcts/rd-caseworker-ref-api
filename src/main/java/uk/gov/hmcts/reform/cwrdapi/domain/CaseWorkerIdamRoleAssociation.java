package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "case_worker_idam_role_assoc")
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "cw_idam_role_assoc_id_seq", sequenceName = "cw_idam_role_assoc_id_seq", allocationSize = 1)
public class CaseWorkerIdamRoleAssociation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cw_idam_role_assoc_id_seq")
    @Column(name = "cw_idam_role_assoc_id")
    private Long cwIdamRoleAssociationId;

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
