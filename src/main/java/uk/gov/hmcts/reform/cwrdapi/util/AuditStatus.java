package uk.gov.hmcts.reform.cwrdapi.util;

public enum AuditStatus {

    IN_PROGRESS("InProgress"),
    FAILURE("Failure"),
    PARTIAL_SUCCESS("Partial Success"),
    SUCCESS("Success");

    private final String status;

    AuditStatus(String  status) {
        this.status  = status;
    }

    public String getStatus() {
        return status;
    }
}
