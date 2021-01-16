package uk.gov.hmcts.reform.cwrdapi.service.impl;

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
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerServiceFacade;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class CaseWorkerServiceFacadeImpl implements CaseWorkerServiceFacade {
    @Autowired
    CaseWorkerService caseWorkerService;

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

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@cwrfunctestuser.com";

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> processFile(MultipartFile file) {
        Workbook workbook = excelValidatorService.validateExcelFile(file);

        Class<? extends CaseWorkerDomain> ob = nonNull(file.getOriginalFilename())
                        && file.getOriginalFilename().startsWith(CaseWorkerConstants.CASE_WORKER_FILE_NAME)
                        ? CaseWorkerProfile.class : ServiceRoleMapping.class;

        List<CaseWorkerDomain> caseWorkerRequest = ( List<CaseWorkerDomain>) excelAdaptorService
                            .parseExcel(workbook, ob);

        List<CaseWorkerDomain> invalidRecords = validationService.getInvalidRecords(caseWorkerRequest);
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
