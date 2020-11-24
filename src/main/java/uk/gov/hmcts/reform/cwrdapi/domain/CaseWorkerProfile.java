package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.AllArgsConstructor;
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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity(name = "case_worker_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseWorkerProfile implements Serializable {

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
    @OneToMany(targetEntity = CaseWorkerLocation.class, mappedBy = "caseWorkerProfile", cascade = CascadeType.ALL)
    private List<CaseWorkerLocation> caseWorkerLocations = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerWorkArea.class, mappedBy = "caseWorkerProfile", cascade = CascadeType.ALL)
    private List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerRole.class, mappedBy = "caseWorkerProfile", cascade = CascadeType.ALL)
    private List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_type_id", referencedColumnName = "user_type_id",
            insertable = false, updatable = false, nullable = false)
    private UserType userType;

    public CaseWorkerProfile(String caseWorkerId, String firstName, String lastName, String emailId, Long userTypeId,
                             Integer regionId, String region, boolean deleteFlag) {
        this.caseWorkerId = caseWorkerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailId = emailId;
        this.userTypeId = userTypeId;
        this.regionId = regionId;
        this.region = region;
        this.deleteFlag = deleteFlag;
    }
}
