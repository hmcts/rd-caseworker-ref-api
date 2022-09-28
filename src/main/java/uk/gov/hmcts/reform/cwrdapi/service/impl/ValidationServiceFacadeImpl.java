package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.AuditRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
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
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@Component
@Slf4j
public class ValidationServiceFacadeImpl implements IValidationService {

    @Autowired
    private IJsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer;

    private CaseWorkerAudit caseWorkerAudit;

    @Autowired
    AuditRepository caseWorkerAuditRepository;

    @Autowired
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;

    @Autowired
    @Lazy
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private long auditJobId;

    List<ExceptionCaseWorker> caseWorkersExceptions;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    StaffAuditRepository staffAuditRepository;

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
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveJsrExceptionsForCaseworkerJob(long jobId) {
        Set<ConstraintViolation<CaseWorkerDomain>> constraintViolationSet
            = jsrValidatorInitializer.getConstraintViolations();
        caseWorkersExceptions = new LinkedList<>();
        AtomicReference<Field> field = new AtomicReference<>();
        //if JSR violation present then only persist exception
        ofNullable(constraintViolationSet).ifPresent(constraintViolations ->
            constraintViolations.forEach(constraintViolation -> {
                log.info("{}:: Invalid JSR for row Id {} in job {} ", loggingComponentName,
                    constraintViolation.getRootBean().getRowId(), jobId);
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
        exceptionCaseWorkerRepository.saveAll(caseWorkersExceptions);
    }


    public String getKeyFieldValue(Field field, CaseWorkerDomain domain) {
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
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long updateCaseWorkerAuditStatus(final AuditStatus auditStatus, final String fileName) {
        createOrUpdateCaseworkerAudit(auditStatus, fileName);
        this.auditJobId = caseWorkerAuditRepository.save(caseWorkerAudit).getJobId();
        return auditJobId;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public long startCaseworkerAuditing(final AuditStatus auditStatus, final String fileName) {
        this.caseWorkerAudit = CaseWorkerAudit.builder().build();
        createOrUpdateCaseworkerAudit(auditStatus, fileName);
        this.auditJobId = caseWorkerAuditRepository.save(caseWorkerAudit).getJobId();
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
    public CaseWorkerAudit createOrUpdateCaseworkerAudit(AuditStatus auditStatus, String fileName) {
        if (isNull(caseWorkerAudit) || isNull(caseWorkerAudit.getJobId())) {
            UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
            String userId = (nonNull(userInfo) && nonNull(userInfo.getUid())) ? userInfo.getUid() : EMPTY;
            caseWorkerAudit = CaseWorkerAudit.builder()
                .status(auditStatus.getStatus())
                .jobStartTime(LocalDateTime.now())
                .fileName(fileName)
                .authenticatedUserId(userId)
                .build();
        } else {
            caseWorkerAudit.setStatus(auditStatus.getStatus());
            caseWorkerAudit.setJobEndTime(LocalDateTime.now());
            caseWorkerAudit.setJobId(getAuditJobId());
        }
        return caseWorkerAudit;
    }


    /**
     * logging User profile failures.
     *
     * @param message String
     * @param rowId   long
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void logFailures(String message, long rowId) {
        log.info("{}:: Failure row Id {} with error {} in job {}  ", loggingComponentName, rowId, message,
            getAuditJobId());

        ExceptionCaseWorker exceptionCaseWorker = createException(getAuditJobId(), message, rowId);
        exceptionCaseWorkerRepository.save(exceptionCaseWorker);
    }

    @Override
    public void saveStaffAudit(AuditStatus auditStatus, String errorMessage,String caseWorkerId,
                               StaffProfileCreationRequest staffProfileCreationRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            if (errorMessage != null && errorMessage.length() > 512) {

                errorMessage = errorMessage.substring(0, 511);
            }

            UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
            String userId = (nonNull(userInfo) && nonNull(userInfo.getUid())) ? userInfo.getUid() : null;

            String request = objectMapper.writeValueAsString(staffProfileCreationRequest);

            StaffAudit staffAudit = StaffAudit.builder()
                        .status(auditStatus.getStatus().toUpperCase())
                        .requestTimeStamp(LocalDateTime.now())
                        .errorDescription(errorMessage)
                        .authenticatedUserId(userId)
                        .caseWorkerId(caseWorkerId)
                        .operationType("CREATE")
                        .requestLog(request)
                        .build();

            staffAuditRepository.save(staffAudit);
        } catch (JsonProcessingException e) {
            log.info("{}:: Failure errorMessager {} in caseworker {}  ", loggingComponentName, errorMessage,
                    caseWorkerId);
        }
    }

    public long getAuditJobId() {
        return auditJobId;
    }
}
