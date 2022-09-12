package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity(name = "skill")
@Getter
@Setter
@NoArgsConstructor

public class Skill implements Serializable {

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "skill_code")
    @Size(max = 62)
    private String skillCode;

    @Column(name = "service_id")
    @Size(max = 64)
    private String serviceId;

    @Column(name = "user_type")
    @Size(max = 512)
    private String userType;

    @Column(name = "description")
    @Size(max = 512)
    private String description;

    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

}
