package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "job_id_seq", sequenceName = "job_id_seq", allocationSize = 1)
@NamedEntityGraph(
        name = "CaseWorkerAudit.alljoins",
        attributeNodes = {
                @NamedAttributeNode(value = "caseWorkerExceptions")
        }
)
public class CaseWorkerAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_id_seq")
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "authenticated_user_id")
    @Size(max = 32)
    private String authenticatedUserId;

    @Column(name = "job_start_time")
    private LocalDateTime jobStartTime;

    @Column(name = "file_name")
    @Size(max = 64)
    private String fileName;

    @Column(name = "job_end_time")
    private LocalDateTime jobEndTime;

    @Column(name = "status")
    @Size(max = 32)
    private String status;

    @Column(name = "comments")
    @Size(max = 512)
    private String comments;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(targetEntity = CaseWorkerException.class, mappedBy = "caseWorkerAudit")
    private List<CaseWorkerException> caseWorkerExceptions = new ArrayList<>();

}
