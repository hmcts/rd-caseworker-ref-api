package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl.CaseWorkerInternalApiClientImpl;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.JsrFileErrors;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileConverter;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DELIMITER_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_FAILED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_FAILED_FILE_UPLOAD_JSR;

@Service
@Slf4j
public class CaseWorkerServiceFacadeImpl implements CaseWorkerServiceFacade {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    ExcelValidatorService excelValidatorService;

    @Autowired
    ExcelAdaptorService excelAdaptorService;

    @Autowired
    CaseWorkerInternalApiClientImpl caseWorkerInternalClient;

    @Autowired
    ValidationServiceFacadeImpl validationServiceFacadeImpl;

    @Autowired
    CaseWorkerProfileConverter caseWorkerProfileConverter;

    @Autowired
    AuditAndExceptionRepositoryServiceImpl auditAndExceptionRepositoryServiceImpl;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> processFile(MultipartFile file) {

        AuditStatus status = SUCCESS;

        try {
            long time1 = System.currentTimeMillis();
            Workbook workbook = excelValidatorService.validateExcelFile(file);
            String fileName = file.getOriginalFilename();
            log.info("{}::Time taken to validate the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time1));

            Class<? extends CaseWorkerDomain> ob = nonNull(fileName)
                && fileName.toLowerCase().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME)
                ? CaseWorkerProfile.class : ServiceRoleMapping.class;

            long time2 = System.currentTimeMillis();
            List<CaseWorkerDomain> caseWorkerRequest = (List<CaseWorkerDomain>) excelAdaptorService
                .parseExcel(workbook, ob);
            log.info("{}::Time taken to parse the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time2));

            long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.IN_PROGRESS, fileName);

            long time3 = System.currentTimeMillis();
            List<CaseWorkerDomain> invalidRecords = validationServiceFacadeImpl.getInvalidRecords(caseWorkerRequest);
            log.info("{}::Time taken to validate the records is {}", loggingComponentName,
                (System.currentTimeMillis() - time3));

            final int totalRecords = isNotEmpty(caseWorkerRequest) ? caseWorkerRequest.size() : 0;

            if (isNotEmpty(invalidRecords)) {
                caseWorkerRequest.removeAll(invalidRecords);
                //audit exceptions or invalid records
                status = PARTIAL_SUCCESS;
                //Inserts JSR exceptions
                validationServiceFacadeImpl.auditJsr(jobId);
            }
            boolean isCaseWorker = false;
            if (isNotEmpty(caseWorkerRequest)) {
                if (caseWorkerRequest.get(0).getClass().isAssignableFrom(CaseWorkerProfile.class)) {

                    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests
                        = caseWorkerProfileConverter.convert(caseWorkerRequest);
                    caseWorkerInternalClient
                        .postRequest(caseWorkersProfileCreationRequests, "/users");
                    isCaseWorker = true;

                } else {
                    caseWorkerInternalClient.postRequest(caseWorkerRequest, "/idam-roles-mapping");
                }
            }

            List<ExceptionCaseWorker> exceptionCaseWorkerList =
                auditAndExceptionRepositoryServiceImpl.getAllExceptions(jobId);

            status = (nonNull(exceptionCaseWorkerList) && (exceptionCaseWorkerList.size()) > 0)
                ? PARTIAL_SUCCESS : status;

            validationServiceFacadeImpl.insertAudit(status, fileName);

            CaseWorkerFileCreationResponse caseWorkerFileCreationResponse =
                createResponse(totalRecords, exceptionCaseWorkerList, isCaseWorker);

            return ResponseEntity.ok().body(caseWorkerFileCreationResponse);

        } catch (Exception ex) {
            long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.FAILURE, file.getOriginalFilename());
            ExceptionCaseWorker exceptionCaseWorker = validationServiceFacadeImpl.createException(jobId,
                ex.getMessage(), 0L);
            auditAndExceptionRepositoryServiceImpl.auditException(exceptionCaseWorker);
            throw ex;
        }
    }

    /**
     * create CaseWorkerFileCreationResponse.
     *
     * @return CaseWorkerFileCreationResponse CaseWorkerFileCreationResponse
     */
    private CaseWorkerFileCreationResponse createResponse(int noOfUploadedRecords,
                                                          List<ExceptionCaseWorker> exceptionCaseWorkerList,
                                                          boolean isCaseWorker) {
        var caseWorkerFileCreationResponseBuilder =
            CaseWorkerFileCreationResponse.builder();

        int suspendedRow = isEmpty(caseWorkerProfileConverter.getSuspendedRowIds()) ? 0 :
            caseWorkerProfileConverter.getSuspendedRowIds().size();

        noOfUploadedRecords = noOfUploadedRecords - suspendedRow;

        if (isNotEmpty(exceptionCaseWorkerList)) {

            Map<String, List<ExceptionCaseWorker>> failedRecords = exceptionCaseWorkerList.stream()
                .collect(groupingBy(ExceptionCaseWorker::getExcelRowId));

            List<JsrFileErrors> jsrFileErrors = new LinkedList<>();

            failedRecords.forEach((key, value) ->
                value.forEach(jsrInvalid ->
                    jsrFileErrors.add(JsrFileErrors.builder().rowId(jsrInvalid.getExcelRowId())
                        .errorDescription(jsrInvalid.getErrorDescription())
                        .filedInError(jsrInvalid.getFieldInError()).build())));

            noOfUploadedRecords = noOfUploadedRecords - failedRecords.size();
            String suspendedRecordMessage = getSuspendedErrorMessageForJsr(isCaseWorker, failedRecords);

            return caseWorkerFileCreationResponseBuilder.message(REQUEST_FAILED_FILE_UPLOAD_JSR)
                .detailedMessage(format(RECORDS_FAILED, failedRecords.size()) + DELIMITER_COMMA + SPACE
                    + recordsUploadMessage(noOfUploadedRecords)
                    + suspendedRecordMessage).errorDetails(jsrFileErrors).build();
        } else {
            String suspendedRecordMessage = getSuspendedErrorMessage(isCaseWorker,
                suspendedRow);

            return caseWorkerFileCreationResponseBuilder
                .message(REQUEST_COMPLETED_SUCCESSFULLY)
                .detailedMessage(recordsUploadMessage(noOfUploadedRecords)
                    + suspendedRecordMessage).build();
        }
    }

    private String recordsUploadMessage(int size) {
        if (size > 0) {
            return format(RECORDS_UPLOADED, size);
        }
        return EMPTY;
    }

    private String getSuspendedErrorMessageForJsr(boolean isCaseWorker,
                                                  Map<String, List<ExceptionCaseWorker>> failedRecords) {
        if (isNotEmpty(caseWorkerProfileConverter.getSuspendedRowIds())) {
            int suspendedFailed = caseWorkerProfileConverter.getSuspendedRowIds().stream()
                .filter(s -> failedRecords.containsKey(Long.toString(s))).collect(toList()).size();
            return getSuspendedErrorMessage(isCaseWorker,
                caseWorkerProfileConverter.getSuspendedRowIds().size() - suspendedFailed);
        }
        return EMPTY;
    }

    private String getSuspendedErrorMessage(boolean isCaseWorker, int suspendedSize) {
        String suspendedRecordMessage = EMPTY;
        if (isCaseWorker && suspendedSize > 0) {
            suspendedRecordMessage = DELIMITER_COMMA + SPACE + format(RECORDS_SUSPENDED, suspendedSize);
        }
        return suspendedRecordMessage;
    }
}
