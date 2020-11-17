package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

@Entity(name = "role_type")
@Getter
@Setter
@NoArgsConstructor
public class RoleType implements Serializable {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerRole.class, mappedBy = "roleType")
    private List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerIdamRoleAssociation.class, mappedBy = "roleType")
    private List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoleAssociations = new ArrayList<>();


}
