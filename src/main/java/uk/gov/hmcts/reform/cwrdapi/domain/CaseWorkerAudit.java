package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "case_worker_audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "job_id_seq", sequenceName = "job_id_seq", allocationSize = 1)
public class CaseWorkerAudit implements Serializable {

    @Id
    @Column(name = "job_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_id_seq")
    private Long jobId;

    @Column(name = "authenticated_user_id")
    @Size(max = 36)
    private String authenticatedUserId;

    @Column(name = "job_start_time")
    private LocalDateTime jobStartTime;

    @Column(name = "file_name")
    @Size(max = 128)
    private String fileName;

    @Column(name = "job_end_time")
    private LocalDateTime jobEndTime;

    @Column(name = "status")
    @Size(max = 32)
    private String status;

    @Column(name = "comments")
    @Size(max = 512)
    private String comments;

    @OneToMany(targetEntity = ExceptionCaseWorker.class, mappedBy = "caseWorkerAudit")
    private List<ExceptionCaseWorker> exceptionCaseWorkers;

}
