package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Slf4j
public class CaseWorkerServiceFacadeImpl implements CaseWorkerServiceFacade {

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
        log.info("----Time taken to validate the given file "
                        + (System.currentTimeMillis() - time1));
        String fileName = file.getOriginalFilename();
        Class<? extends CaseWorkerDomain> ob = fileName != null
                        && fileName.toLowerCase().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME)
                        ? CaseWorkerProfile.class : ServiceRoleMapping.class;
        long time2 = System.currentTimeMillis();
        List<CaseWorkerDomain> caseWorkerRequest = ( List<CaseWorkerDomain>) excelAdaptorService
                            .parseExcel(workbook, ob);
        log.info("----Time taken to parse the given file "
                + (System.currentTimeMillis() - time2));
        long time3 = System.currentTimeMillis();
        List<CaseWorkerDomain> invalidRecords = validationService.getInvalidRecords(caseWorkerRequest);
        log.info("----Time taken to validate the records "
                + (System.currentTimeMillis() - time3));
        if (!invalidRecords.isEmpty()) {
            caseWorkerRequest.removeAll(invalidRecords);
        }

        if (caseWorkerRequest.get(0).getClass().isAssignableFrom(CaseWorkerProfile.class)) {
            List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerProfileConverter
                    .convert(caseWorkerRequest);
            return caseWorkerInternalClient
                    .postRequest(caseWorkersProfileCreationRequests, "/users");
        } else {
            return caseWorkerInternalClient.postRequest(caseWorkerRequest,"/idam-roles-mapping");
        }
    }
}
