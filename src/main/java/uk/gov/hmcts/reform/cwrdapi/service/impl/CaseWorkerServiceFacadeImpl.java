package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;

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

    @Autowired
    CaseWorkerServiceImpl caseWorkerServiceImpl;

    @Autowired
    ObjectMapper objectMapper;


    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> processFile(MultipartFile file) {


        ResponseEntity<Object> responseEntity = ResponseEntity.noContent().build();
        AuditStatus status = SUCCESS;

        try {

            long time1 = System.currentTimeMillis();
            Workbook workbook = excelValidatorService.validateExcelFile(file);
            String fileName = file.getOriginalFilename();
            log.info("{}::Time taken to validate the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time1));

            Class<? extends CaseWorkerDomain> ob = file.getOriginalFilename() != null
                && file.getOriginalFilename().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME)
                ? CaseWorkerProfile.class : ServiceRoleMapping.class;


            long time2 = System.currentTimeMillis();
            List<CaseWorkerDomain> caseWorkerRequest = (List<CaseWorkerDomain>) excelAdaptorService
                .parseExcel(workbook, ob);
            log.info("{}::Time taken to parse the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time2));

            long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.IN_PROGRESS, file.getName());

            long time3 = System.currentTimeMillis();
            List<CaseWorkerDomain> invalidRecords = validationServiceFacadeImpl.getInvalidRecords(caseWorkerRequest);
            log.info("{}::Time taken to validate the records is {}", loggingComponentName,
                (System.currentTimeMillis() - time3));

            final int totalRecords = nonNull(caseWorkerRequest) ? caseWorkerRequest.size() : 0;
            if (isNotEmpty(invalidRecords)) {
                caseWorkerRequest.removeAll(invalidRecords);
                //audit exceptions or invalid records
                status = PARTIAL_SUCCESS;
                //Inserts JSR exceptions
                validationServiceFacadeImpl.auditJsr(jobId);
            }
            if (isNotEmpty(caseWorkerRequest)) {
                if (caseWorkerRequest.get(0).getClass().isAssignableFrom(CaseWorkerProfile.class)) {

                    //            List<CaseWorkerDomain> suspendedCaseworkers =
                    //            caseWorkerRequest.stream().filter(s -> ((CaseWorkerProfile) s)
                    //            .getSuspended().equals("Y"))
                    //                       .collect(Collectors.toList());
                    //             suspendedSize = (nonNull(suspendedCaseworkers)) ? suspendedCaseworkers.size()
                    //             : suspendedSize;

                    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests
                        = caseWorkerProfileConverter.convert(caseWorkerRequest);
                    caseWorkerInternalClient
                        .postRequest(caseWorkersProfileCreationRequests, "/users");
                } else {
                    caseWorkerInternalClient.postRequest(caseWorkerRequest, "/idam-roles-mapping");
                }
            }

            List<ExceptionCaseWorker> exceptionCaseWorkerList =
                auditAndExceptionRepositoryServiceImpl.getAllExceptions(jobId);

            status = (nonNull(exceptionCaseWorkerList) && (exceptionCaseWorkerList.size()) > 0)
                ? PARTIAL_SUCCESS : status;

            validationServiceFacadeImpl.insertAudit(status, file.getName());

            CaseWorkerFileCreationResponse caseWorkerFileCreationResponse =
                createResponse(totalRecords, exceptionCaseWorkerList);

            return ResponseEntity.ok().body(caseWorkerFileCreationResponse);

        } catch (Exception ex) {
            long jobId = validationServiceFacadeImpl.insertAudit(AuditStatus.FAILURE, file.getName());
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
                                                          List<ExceptionCaseWorker> exceptionCaseWorkerList) {
        var caseWorkerFileCreationResponseBuilder =
            CaseWorkerFileCreationResponse.builder();


        if (nonNull(exceptionCaseWorkerList) && exceptionCaseWorkerList.size() > 0) {

            Map<String, List<ExceptionCaseWorker>> failedRecords = exceptionCaseWorkerList.stream()
                .collect(groupingBy(ExceptionCaseWorker::getExcelRowId));

            List<JsrFileErrors> jsrFileErrors = new LinkedList<>();

            failedRecords.entrySet().stream().forEach(error -> {
                error.getValue().stream().forEach(jsrInvalid -> {
                    jsrFileErrors.add(JsrFileErrors.builder().rowId(jsrInvalid.getExcelRowId())
                        .errorDescription(jsrInvalid.getErrorDescription())
                        .filedInError(jsrInvalid.getFieldInError()).build());
                });
            });

            noOfUploadedRecords = noOfUploadedRecords - failedRecords.size();

            return caseWorkerFileCreationResponseBuilder.message("Request completed with partial success. Some records "
                + "failed during validation and were ignored.")
                .detailedMessage(failedRecords.size() + " record(s) failed validation, " + noOfUploadedRecords
                    + " record(s) uploaded").errorDetails(jsrFileErrors).build();

        } else {
            return caseWorkerFileCreationResponseBuilder
                .message("Request Completed Successfully")
                .detailedMessage(noOfUploadedRecords + " record(s) uploaded").build();
        }
    }
}
