package uk.gov.hmcts.reform.cwrdapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


@Entity(name = "user_type")
@Getter
@Setter
@NoArgsConstructor
public class UserType implements Serializable {

    @Id
    @Column(name = "user_type_id")
    private  Long userTypeId;

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
    @JsonIgnore
    @OneToMany(targetEntity = CaseWorkerProfile.class, mappedBy = "userType")
    private List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();

}
