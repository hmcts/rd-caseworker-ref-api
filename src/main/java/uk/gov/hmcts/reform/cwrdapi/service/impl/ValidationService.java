package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.service.IAuditService;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

import static java.util.Optional.ofNullable;

@Component
public class ValidationService implements IValidationService {

    @Autowired
    IJsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Autowired
    IAuditService auditService;

    /**
     * Returns invalid record list and JSR Constraint violations pair.
     *
     * @param caseWorkerProfileList List
     * @return CasWorkerDomain list
     */
    public List<CaseWorkerDomain> getInvalidRecords(List<CaseWorkerDomain> caseWorkerProfileList) {
        //Gets Invalid records
        return jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfileList);
    }

    /**
     * Audit JSR exceptions..
     *
     * @param jobId long
     */
    public void auditJsr(long jobId) {
        Set<ConstraintViolation<CaseWorkerDomain>> constraintViolationSet
            = jsrValidatorInitializer.getConstraintViolations();
        List<ExceptionCaseWorker> exceptionCaseWorkers = new ArrayList<>();

        //if JSR violation present then only persist exception
        ofNullable(constraintViolationSet).ifPresent(constraintViolations ->
            constraintViolations.forEach(constraintViolation -> {
                ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
                exceptionCaseWorker.setJobId(jobId);
                exceptionCaseWorker.setFieldInError(constraintViolation.getPropertyPath().toString());
                exceptionCaseWorker.setErrorDescription(constraintViolation.getMessage());
                exceptionCaseWorker.setExcelRowId(String.valueOf(constraintViolation.getRootBean().getRowId()));
                exceptionCaseWorker.setUpdatedTimeStamp(LocalDateTime.now());
                exceptionCaseWorkers.add(exceptionCaseWorker);
            }));
        auditService.auditException(exceptionCaseWorkers);
        //@To do set Audit JOB status to Partial Success in Request Session
    }
}
