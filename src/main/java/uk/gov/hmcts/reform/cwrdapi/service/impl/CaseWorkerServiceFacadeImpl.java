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
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileConverter;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.List;
import java.util.Objects;

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
    ValidationService validationService;

    @Autowired
    CaseWorkerProfileConverter caseWorkerProfileConverter;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> processFile(MultipartFile file) {
        long time1 = System.currentTimeMillis();
        Workbook workbook = excelValidatorService.validateExcelFile(file);
        String fileName = file.getOriginalFilename();
        log.info("{}::Time taken to validate the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time1));

        Class<? extends CaseWorkerDomain> ob = Objects.nonNull(fileName)
                        && fileName.toLowerCase().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME)
                        ? CaseWorkerProfile.class : ServiceRoleMapping.class;

        long time2 = System.currentTimeMillis();
        List<CaseWorkerDomain> caseWorkerRequest = ( List<CaseWorkerDomain>) excelAdaptorService
                            .parseExcel(workbook, ob);
        log.info("{}::Time taken to parse the given file {} is {}",
                loggingComponentName, fileName, (System.currentTimeMillis() - time2));

        long time3 = System.currentTimeMillis();
        List<CaseWorkerDomain> invalidRecords = validationService.getInvalidRecords(caseWorkerRequest);
        log.info("{}::Time taken to validate the records is {}", loggingComponentName,
                (System.currentTimeMillis() - time3));
        if (!invalidRecords.isEmpty()) {
            caseWorkerRequest.removeAll(invalidRecords);
        }

        if (!caseWorkerRequest.isEmpty()) {
            if (caseWorkerRequest.get(0).getClass().isAssignableFrom(CaseWorkerProfile.class)) {
                List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerProfileConverter
                        .convert(caseWorkerRequest);
                return caseWorkerInternalClient
                        .postRequest(caseWorkersProfileCreationRequests, "/users");
            } else {
                return caseWorkerInternalClient.postRequest(caseWorkerRequest, "/idam-roles-mapping");
            }
        }
        return null;
    }
}
