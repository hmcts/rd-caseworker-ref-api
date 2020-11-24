package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.JsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ValidationService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.util.ReflectionUtils.setField;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.buildCaseWorkerProfileData;

public class ValidationServiceTest {

    ValidationService validationService = spy(new ValidationService());

    AuditService auditService = spy(new AuditService());

    @Before
    public void init() throws Exception {
        Field auditServiceField = validationService.getClass().getDeclaredField("auditService");
        auditServiceField.setAccessible(true);
        setField(auditServiceField, validationService, auditService);
    }

    @Test
    public void testGetInvalidRecords() throws Exception {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        Field validationField = validationService.getClass().getDeclaredField("jsrValidatorInitializer");
        validationField.setAccessible(true);
        setField(validationField, validationService, jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = buildCaseWorkerProfileData();
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);

        validationService.getInvalidRecords(caseWorkerProfiles);
        verify(validationService).getInvalidRecords(caseWorkerProfiles);
    }

    @Test
    public void testAuditJsr() throws Exception {
        JsrValidatorInitializer<CaseWorkerDomain> jsrValidatorInitializer = new JsrValidatorInitializer<>();
        jsrValidatorInitializer.initializeFactory();
        Field validationField = validationService.getClass().getDeclaredField("jsrValidatorInitializer");
        validationField.setAccessible(true);
        setField(validationField, validationService, jsrValidatorInitializer);
        List<CaseWorkerDomain> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile profile = new CaseWorkerProfile();
        caseWorkerProfiles.add(profile);
        jsrValidatorInitializer.getInvalidJsrRecords(caseWorkerProfiles);
        doNothing().when(auditService).auditException(anyList());
        validationService.auditJsr(1);
        verify(validationService).auditJsr(1);
    }
}
