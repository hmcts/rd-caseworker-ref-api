package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "role_type")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NamedEntityGraph(
        name = "RoleType.alljoins",
        attributeNodes = {
                @NamedAttributeNode(value = "caseWorkerRoles"),
                @NamedAttributeNode(value = "caseWorkerIDAMRoleAssociations")
        }
)
public class RoleType implements Serializable {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerRole.class, mappedBy = "roleType")
    private List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerIDAMRoleAssociation.class, mappedBy = "roleType")
    private List<CaseWorkerIDAMRoleAssociation> caseWorkerIDAMRoleAssociations = new ArrayList<>();
}
