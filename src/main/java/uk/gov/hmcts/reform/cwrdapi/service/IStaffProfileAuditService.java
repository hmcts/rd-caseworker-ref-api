package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;

public interface IStaffProfileAuditService {

    void saveStaffAudit(AuditStatus auditStatus, final String errorMessage, String caseWorkerId,
                        StaffProfileCreationRequest request, String operationType);
}
