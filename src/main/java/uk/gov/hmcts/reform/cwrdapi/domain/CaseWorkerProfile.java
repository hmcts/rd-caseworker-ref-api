package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity(name = "case_worker_profile")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@NamedEntityGraph(
        name = "CaseWorkerProfile.alljoins",
        attributeNodes = {
                @NamedAttributeNode(value = "caseWorkerLocations"),
                @NamedAttributeNode(value = "caseWorkerWorkAreas"),
                @NamedAttributeNode(value = "caseWorkerRoles"),
        }
)
public class CaseWorkerProfile implements Serializable {
    @Column(name = "case_worker_id")
    @Size(max = 64)
    private String caseWorkerId;

    @Column(name = "first_name")
    @Size(max = 128)
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 128)
    private String lastName;

    @Column(name = "email_id", unique = true)
    @Size(max = 512)
    @NotNull
    private String emailId;

    @Column(name = "user_type_id")
    @NotNull
    private Long userTypeId;

    @Size(max = 64)
    @Column(name = "region")
    private String region;

    @Column(name = "region_id")
    @NotNull
    private Integer regionId;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerLocation.class, mappedBy = "caseWorkerProfile")
    private List<CaseWorkerLocation> caseWorkerLocations = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerWorkArea.class, mappedBy = "caseWorkerProfile")
    private List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerRole.class, mappedBy = "caseWorkerProfile")
    private List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_type_id", referencedColumnName = "user_type_id",
            insertable = false, updatable = false, nullable = false)
    private UserType userType;
}
