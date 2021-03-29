package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.CascadeType.ALL;


@Entity(name = "case_worker_profile")
@Getter
@Setter
@NoArgsConstructor
public class CaseWorkerProfile implements Persistable {

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

    private transient boolean isNew = false;

    @Override
    public Object getId() {
        return caseWorkerId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    //Added this as lombok can't generate some args constructor and we have to exclude isNew field
    public CaseWorkerProfile(final String caseWorkerId, final String firstName, final String lastName,
                             final String emailId, final Long userTypeId, final String region,
                             final Integer regionId, final Boolean suspended, final LocalDateTime createdDate,
                             final LocalDateTime lastUpdate, final List<CaseWorkerLocation> caseWorkerLocations,
                             final List<CaseWorkerWorkArea> caseWorkerWorkAreas,
                             final List<CaseWorkerRole> caseWorkerRoles,
                             final UserType userType) {
        this.caseWorkerId = caseWorkerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailId = emailId;
        this.userTypeId = userTypeId;
        this.region = region;
        this.regionId = regionId;
        this.suspended = suspended;
        this.createdDate = createdDate;
        this.lastUpdate = lastUpdate;
        this.caseWorkerLocations = caseWorkerLocations;
        this.caseWorkerWorkAreas = caseWorkerWorkAreas;
        this.caseWorkerRoles = caseWorkerRoles;
        this.userType = userType;
    }

}
