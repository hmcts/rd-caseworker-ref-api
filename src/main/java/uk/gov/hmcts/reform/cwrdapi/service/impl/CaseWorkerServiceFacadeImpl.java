package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl.CaseWorkerInternalApiClientImpl;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.JsrFileErrors;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileConverter;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.AND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DELIMITER_COMMA;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.MULTIPLE_SERVICE_CODES;
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
    IValidationService validationServiceFacadeImpl;

    @Autowired
    CaseWorkerProfileConverter caseWorkerProfileConverter;

    @Autowired
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;


    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> processFile(MultipartFile file) {

        AuditStatus status = SUCCESS;

        try {
            long jobId = validationServiceFacadeImpl.getAuditJobId();
            long time1 = System.currentTimeMillis();
            Workbook workbook = excelValidatorService.validateExcelFile(file);
            String fileName = file.getOriginalFilename();
            log.info("{}::Time taken to validate the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time1));

            boolean isCaseWorker = nonNull(fileName)
                    && fileName.toLowerCase().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME);

            Class<? extends CaseWorkerDomain> ob = isCaseWorker
                ? CaseWorkerProfile.class : ServiceRoleMapping.class;

            long time2 = System.currentTimeMillis();
            List<CaseWorkerDomain> caseWorkerRequest = (List<CaseWorkerDomain>) excelAdaptorService
                .parseExcel(workbook, ob);

            log.info("{}::Time taken to parse the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time2));

            long time3 = System.currentTimeMillis();
            List<CaseWorkerDomain> invalidRecords = validationServiceFacadeImpl.getInvalidRecords(caseWorkerRequest);
            log.info("{}::Time taken to validate the records is {}", loggingComponentName,
                (System.currentTimeMillis() - time3));

            int totalRecords = isNotEmpty(caseWorkerRequest) ? caseWorkerRequest.size() : 0;

            if (isNotEmpty(invalidRecords)) {
                caseWorkerRequest.removeAll(invalidRecords);
                //audit exceptions or invalid records
                status = PARTIAL_SUCCESS;
                //Inserts JSR exceptions
                validationServiceFacadeImpl.saveJsrExceptionsForCaseworkerJob(jobId);
            }
            if (isNotEmpty(caseWorkerRequest)) {
                if (isCaseWorker) {
                    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests
                        = caseWorkerProfileConverter.convert(caseWorkerRequest);
                    caseWorkerInternalClient
                        .postRequest(caseWorkersProfileCreationRequests, "/users");
                } else {
                    validateServiceRoleMappingSheet(caseWorkerRequest);
                    caseWorkerInternalClient.postRequest(caseWorkerRequest, "/idam-roles-mapping");
                }
            }

            return sendResponse(file, status, isCaseWorker, totalRecords);

        } catch (Exception ex) {

            long jobId = validationServiceFacadeImpl.updateCaseWorkerAuditStatus(AuditStatus.FAILURE,
                file.getOriginalFilename());
            validationServiceFacadeImpl.logFailures(ex.getMessage(), 0L);
            log.error("{}:: Failed File Upload for job {}", loggingComponentName, jobId);
            throw ex;
        }
    }

    public ResponseEntity<Object> sendResponse(MultipartFile file, AuditStatus status,
                                                Boolean isCaseWorker, int totalRecords) {
        List<ExceptionCaseWorker> exceptionCaseWorkerList = exceptionCaseWorkerRepository
            .findByJobId(validationServiceFacadeImpl.getAuditJobId());
        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse =
            createResponse(totalRecords, exceptionCaseWorkerList, isCaseWorker);
        status = (nonNull(exceptionCaseWorkerList) && (exceptionCaseWorkerList.size()) > 0)
            ? PARTIAL_SUCCESS : status;
        long jobId = validationServiceFacadeImpl.updateCaseWorkerAuditStatus(status, file.getOriginalFilename());
        log.info("{}:: Completed File Upload for Job {} with status", loggingComponentName, jobId, status);
        return ResponseEntity.ok().body(caseWorkerFileCreationResponse);
    }

    private void validateServiceRoleMappingSheet(List<CaseWorkerDomain> caseWorkerRequestList) {
        boolean multipleServiceCode = caseWorkerRequestList.stream()
                .filter(ServiceRoleMapping.class::isInstance)
                .map(ServiceRoleMapping.class::cast)
                .map(ServiceRoleMapping::getServiceId)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .count() > 1;

        if (multipleServiceCode) {
            throw new InvalidRequestException(MULTIPLE_SERVICE_CODES);
        }
    }

    /**
     * create CaseWorkerFileCreationResponse.
     *
     * @return CaseWorkerFileCreationResponse CaseWorkerFileCreationResponse
     */
    private CaseWorkerFileCreationResponse createResponse(int totalRecords,
                                                          List<ExceptionCaseWorker> exceptionCaseWorkerList,
                                                          boolean isCaseWorker) {
        var caseWorkerFileCreationResponseBuilder =
            CaseWorkerFileCreationResponse.builder();

        int suspendedRecords = isCaseWorker && isNotEmpty(caseWorkerProfileConverter.getSuspendedRowIds())
                ? caseWorkerProfileConverter.getSuspendedRowIds().size() : 0;

        if (isNotEmpty(exceptionCaseWorkerList)) {

            Map<String, List<ExceptionCaseWorker>> failedRecords = exceptionCaseWorkerList.stream()
                .collect(groupingBy(ExceptionCaseWorker::getExcelRowId));

            LinkedList<JsrFileErrors> jsrFileErrors = new LinkedList<>();

            failedRecords.entrySet().stream()
                .sorted(Comparator.comparingInt(s -> Integer.valueOf(s.getKey())))
                .forEachOrdered(map ->
                    map.getValue().forEach(jsrInvalid ->
                        jsrFileErrors.add(JsrFileErrors.builder().rowId(jsrInvalid.getExcelRowId())
                            .errorDescription(jsrInvalid.getErrorDescription())
                            .filedInError(jsrInvalid.getFieldInError()).build())));

            String detailedMessage = constructDetailedMessage(totalRecords, suspendedRecords, failedRecords);
            return caseWorkerFileCreationResponseBuilder.message(REQUEST_FAILED_FILE_UPLOAD_JSR)
                .detailedMessage(detailedMessage).errorDetails(jsrFileErrors).build();
        } else {
            StringJoiner detailedMessage = new StringJoiner(SPACE + AND + SPACE);
            //get the uploaded records excluding suspended records
            int noOfUploadedRecords = totalRecords - suspendedRecords;

            if (noOfUploadedRecords > 0) {
                detailedMessage.add(format(RECORDS_UPLOADED, noOfUploadedRecords));
            }
            if (suspendedRecords > 0) {
                detailedMessage.add(format(RECORDS_SUSPENDED, suspendedRecords));
            }
            return caseWorkerFileCreationResponseBuilder
                .message(REQUEST_COMPLETED_SUCCESSFULLY)
                .detailedMessage(detailedMessage.toString()).build();
        }
    }

    private String constructDetailedMessage(int totalRecords, int suspendedRecords, Map<String,
        List<ExceptionCaseWorker>> failedRecords) {
        String detailedMessage = format(RECORDS_FAILED, failedRecords.size());
        //get the uploaded records excluding failed records
        int uploadedRecords = totalRecords - failedRecords.size();
        if (suspendedRecords > 0) {
            //sometimes the suspended records might have other failures
            //so we need to exclude them as it goes under failed records
            suspendedRecords = calculateFinalSuspendedRecords(failedRecords);
            //get the uploaded records excluding suspended records
            uploadedRecords = uploadedRecords - suspendedRecords;
        }
        if (uploadedRecords > 0 && suspendedRecords > 0) {
            detailedMessage = format(RECORDS_FAILED, failedRecords.size()) + DELIMITER_COMMA + SPACE
                .concat(format(RECORDS_UPLOADED, uploadedRecords) + DELIMITER_COMMA + SPACE + AND + SPACE)
                .concat(format(RECORDS_SUSPENDED, suspendedRecords));
        } else if (uploadedRecords > 0) {
            detailedMessage = format(RECORDS_FAILED, failedRecords.size()) + SPACE + AND + SPACE
                .concat(format(RECORDS_UPLOADED, uploadedRecords));
        } else if (suspendedRecords > 0) {
            detailedMessage = format(RECORDS_FAILED, failedRecords.size()) + SPACE + AND + SPACE
                .concat(format(RECORDS_SUSPENDED, suspendedRecords));
        }
        return detailedMessage;
    }


    private int calculateFinalSuspendedRecords(Map<String, List<ExceptionCaseWorker>> failedRecords) {
        int suspendedFailedRecords = (int) caseWorkerProfileConverter.getSuspendedRowIds().stream()
            .filter(s -> failedRecords.containsKey(Long.toString(s))).count();

        return caseWorkerProfileConverter.getSuspendedRowIds().size() - suspendedFailedRecords;
    }
}
