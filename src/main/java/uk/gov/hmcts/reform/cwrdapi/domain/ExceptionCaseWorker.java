package uk.gov.hmcts.reform.cwrdapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "case_worker_exception")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "exception_id_seq", sequenceName = "exception_id_seq", allocationSize = 1)
@Builder
public class ExceptionCaseWorker implements Serializable {

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

    @Column(name = "key")
    @Size(max = 256)
    private String keyField;

    @Column(name = "field_in_error")
    @Size(max = 32)
    private String fieldInError;

    @Column(name = "error_description")
    @Size(max = 2000)
    private String errorDescription;

    @UpdateTimestamp
    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimeStamp;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "job_id",
        insertable = false, updatable = false, nullable = false)
    private CaseWorkerAudit caseWorkerAudit;
}