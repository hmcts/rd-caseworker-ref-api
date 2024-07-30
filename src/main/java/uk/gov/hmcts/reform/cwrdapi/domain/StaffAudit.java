package uk.gov.hmcts.reform.cwrdapi.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "staff_audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "staff_audit_id_seq", sequenceName = "staff_audit_id_seq", allocationSize = 1)
@Convert(attributeName = "jsonb", converter = JsonBinaryType.class)
public class StaffAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_audit_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "authenticated_user_id")
    private String authenticatedUserId;

    @Column(name = "request_timestamp")
    private LocalDateTime requestTimeStamp;

    @Column(name = "status")
    private String status;

    @Column(name = "error_description")
    @Size(max = 512)
    private String errorDescription;

    @Column(name = "case_worker_id")
    private String caseWorkerId;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "request_log",columnDefinition = "json")
    @Convert(converter = JsonType.class)
    private String requestLog;

}
