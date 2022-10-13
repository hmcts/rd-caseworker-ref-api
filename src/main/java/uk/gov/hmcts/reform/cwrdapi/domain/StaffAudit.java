package uk.gov.hmcts.reform.cwrdapi.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;

@Entity(name = "staff_audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "staff_audit_id_seq", sequenceName = "staff_audit_id_seq", allocationSize = 1)
@TypeDefs(@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class))
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
    @Type(type = "jsonb")
    private String requestLog;

}
