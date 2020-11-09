package uk.gov.hmcts.reform.cwrdapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "case_worker_exception")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "exception_id_seq", sequenceName = "exception_id_seq", allocationSize = 1)
public class CaseWorkerException implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exception_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "job_id")
    @NotNull
    private Long jobId;

    @Column(name = "excel_row_id")
    @Size(max = 32)
    private String excelRowId;

    @Column(name = "email_id")
    @Size(max = 32)
    private String emailId;

    @Column(name = "field_in_error")
    @Size(max = 32)
    private String fieldInError;

    @Column(name = "error_description")
    @Size(max = 512)
    private String errorDescription;

    @UpdateTimestamp
    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimeStamp;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id",
            insertable = false, updatable = false, nullable = false)
    private CaseWorkerAudit caseWorkerAudit;
}
