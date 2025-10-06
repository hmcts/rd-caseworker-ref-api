package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;


@Entity(name = "case_worker_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerProfile implements Persistable<String>, Serializable {

    @Id
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

    @Column(name = "suspended")
    private Boolean suspended;

    @Column(name = "case_allocator")
    private Boolean caseAllocator;

    @Column(name = "task_supervisor")
    private Boolean taskSupervisor;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerLocation.class, mappedBy = "caseWorkerProfile", cascade = ALL,
        orphanRemoval = true)
    private List<CaseWorkerLocation> caseWorkerLocations = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerWorkArea.class, mappedBy = "caseWorkerProfile", cascade = ALL,
        orphanRemoval = true)
    private List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerRole.class, mappedBy = "caseWorkerProfile", cascade = ALL, orphanRemoval = true)
    private List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_type_id", referencedColumnName = "user_type_id",
        insertable = false, updatable = false, nullable = false)
    private UserType userType;

    @Transient
    private boolean isNew = false;

    @Column(name = "user_admin")
    private Boolean userAdmin;

    @Override
    public String getId() {
        return caseWorkerId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerSkill.class, mappedBy = "caseWorkerProfile", cascade = ALL,
            orphanRemoval = true)
    private List<CaseWorkerSkill> caseWorkerSkills = new ArrayList<>();
}
