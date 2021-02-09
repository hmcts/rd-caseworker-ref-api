package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.service.IAuditAndExceptionRepositoryService;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.MappingField;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.ConstraintViolation;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@Component
public class ValidationServiceFacadeImpl implements IValidationService {

    @Autowired
    private IJsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    @Autowired
    private IAuditAndExceptionRepositoryService auditAndExceptionRepositoryService;

    private CaseWorkerAudit caseWorkerAudit;

    @Autowired
    @Lazy
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private long auditJobId;

    List<ExceptionCaseWorker> caseWorkersExceptions;


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
     * Audit JSR exceptions.
     *
     * @param jobId long
     */
    public void saveJsrExceptionsForCaseworkerJob(long jobId) {
        Set<ConstraintViolation<CaseWorkerDomain>> constraintViolationSet
            = jsrValidatorInitializer.getConstraintViolations();
        caseWorkersExceptions = new LinkedList<>();
        AtomicReference<Field> field = new AtomicReference<>();
        //if JSR violation present then only persist exception
        ofNullable(constraintViolationSet).ifPresent(constraintViolations ->
            constraintViolations.forEach(constraintViolation -> {
                if (isNull(field.get())) {
                    field.set(getKeyFiled(constraintViolation.getRootBean()).get());
                    ReflectionUtils.makeAccessible(field.get());
                }
                ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
                exceptionCaseWorker.setJobId(jobId);
                exceptionCaseWorker.setFieldInError(constraintViolation.getPropertyPath().toString());
                exceptionCaseWorker.setErrorDescription(constraintViolation.getMessage());
                exceptionCaseWorker.setExcelRowId(String.valueOf(constraintViolation.getRootBean().getRowId()));
                exceptionCaseWorker.setUpdatedTimeStamp(LocalDateTime.now());
                exceptionCaseWorker.setKeyField(getKeyFieldValue(field.get(), constraintViolation.getRootBean()));
                caseWorkersExceptions.add(exceptionCaseWorker);
            }));
        auditAndExceptionRepositoryService.auditException(caseWorkersExceptions);
    }


    private String getKeyFieldValue(Field field, CaseWorkerDomain domain) {
        try {
            return (String) field.get(domain);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage()); //@T0 DO replace IllegalArgumentException
        }
    }

    /**
     * get key fields.
     *
     * @param bean Object
     * @return Field Field
     */
    @SuppressWarnings("unchecked")
    private Optional<Field> getKeyFiled(CaseWorkerDomain bean) {
        Class<CaseWorkerDomain> objectClass = (Class<CaseWorkerDomain>) bean.getClass();
        Optional<Field> field = stream(objectClass.getDeclaredFields()).filter(fld ->
            nonNull(findAnnotation(fld,
                MappingField.class)) && findAnnotation(fld,
                MappingField.class).position() == 1).findFirst();
        return (field.isPresent()) ? field : Optional.empty();
    }

    /**
     * Inserts Audit details in Audit table.
     *
     * @param auditStatus AuditStatus
     * @param fileName    String
     * @return long id
     */
    public long updateCaseWorkerAuditStatus(final AuditStatus auditStatus, final String fileName) {
        createOrUpdateCaseworkerAudit(auditStatus, fileName);
        auditJobId = auditAndExceptionRepositoryService.auditSchedulerStatus(caseWorkerAudit);
        return auditJobId;
    }

    public long startCaseworkerAuditing(final AuditStatus auditStatus, final String fileName) {
        this.caseWorkerAudit = CaseWorkerAudit.builder().build();
        createOrUpdateCaseworkerAudit(auditStatus, fileName);
        this.auditJobId = auditAndExceptionRepositoryService.auditSchedulerStatus(caseWorkerAudit);
        return auditJobId;
    }

    /**
     * Create ExceptionCaseWorker domain object.
     *
     * @param jobId   long
     * @param message String
     * @return ExceptionCaseWorker ExceptionCaseWorker
     */
    public ExceptionCaseWorker createException(final long jobId, final String message, final Long rowId) {
        return ExceptionCaseWorker.builder().jobId(jobId)
            .excelRowId((rowId != 0) ? rowId.toString() : EMPTY)
            .errorDescription(message).updatedTimeStamp(LocalDateTime.now()).build();
    }

    /**
     * Create/Updates CaseWorkerAudit domain object.
     *
     * @param auditStatus AuditStatus
     * @param fileName    String
     * @return CaseWorkerAudit CaseWorkerAudit
     */
    private CaseWorkerAudit createOrUpdateCaseworkerAudit(AuditStatus auditStatus, String fileName) {
        if (isNull(caseWorkerAudit) || isNull(caseWorkerAudit.getJobId())) {
            UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
            String userName = (nonNull(userInfo) && nonNull(userInfo.getName())) ? userInfo.getName() : EMPTY;
            caseWorkerAudit = CaseWorkerAudit.builder()
                .status(auditStatus.getStatus())
                .jobStartTime(LocalDateTime.now())
                .fileName(fileName)
                .authenticatedUserId(userName)
                .build();
        } else {
            caseWorkerAudit.setStatus(auditStatus.getStatus());
            caseWorkerAudit.setJobEndTime(LocalDateTime.now());
            caseWorkerAudit.setJobId(getAuditJobId());
        }
        return caseWorkerAudit;
    }

    public List<ExceptionCaseWorker> getCaseWorkersExceptions() {
        return caseWorkersExceptions;
    }

    public long getAuditJobId() {
        return auditJobId;
    }
}
