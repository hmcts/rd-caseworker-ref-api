package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CasWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

@Component
public class ValidationService {

    @Autowired
    JsrValidatorInitializer<CasWorkerDomain> jsrValidatorInitializer;

    @Autowired
    AuditService auditService;

    /**
     * Returns invalid record list and JSR Constraint violations pair.
     *
     * @param caseWorkerProfileList List
     * @return CasWorkerDomain list
     */
    public List<CasWorkerDomain> getInvalidRecords(List<CasWorkerDomain> caseWorkerProfileList) {
        //Gets Invalid records
        List<CasWorkerDomain> invalidRecords = jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfileList);
        return invalidRecords;
    }

    /**
     * Audit JSR exceptions..
     *
     * @param jobId long
     */
    public void auditJsr(long jobId) {
        Set<ConstraintViolation<CasWorkerDomain>> constraintViolationSet
            = jsrValidatorInitializer.getConstraintViolations();
        List<ExceptionCaseWorker> exceptionCaseWorkers = new ArrayList<>();
        //if JSR violation present then only persist exception
        if (constraintViolationSet != null && constraintViolationSet.size() > 0) {
            constraintViolationSet.stream().forEach(constraintViolation -> {
                ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
                exceptionCaseWorker.setJobId(jobId);
                exceptionCaseWorker.setFieldInError(constraintViolation.getPropertyPath().toString());
                exceptionCaseWorker.setErrorDescription(constraintViolation.getMessage());
                exceptionCaseWorker.setExcelRowId(String.valueOf(constraintViolation.getRootBean().getRowId()));
                exceptionCaseWorker.setUpdatedTimeStamp(LocalDateTime.now());
                exceptionCaseWorkers.add(exceptionCaseWorker);
            });
            auditService.auditException(exceptionCaseWorkers);
            //@To do set Audit JOB status to Partial Success in Request Session
        }
    }
}
