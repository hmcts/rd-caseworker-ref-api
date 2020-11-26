package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IdamRoleMappingServiceImplTest {
    @Mock
    CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;

    @InjectMocks
    IdamRoleMappingServiceImpl idamRoleMappingService;

    @Test
    public void test_saveAll() {

        idamRoleMappingService.buildIdamRoleAssociation(anyList());

        verify(caseWorkerIdamRoleAssociationRepository, times(1))
                .saveAll(anyCollection());
    }

    @Test
    public void test_delete_record_for_service_code() {
        idamRoleMappingService.deleteExistingRecordForServiceCode(anySet());

        verify(caseWorkerIdamRoleAssociationRepository, times(1))
                .deleteByServiceCodeIn(anySet());
    }
}