package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
class StaffRefDataJobTitleTest {
    @Mock
    private RoleTypeRepository roleTypeRepository;

    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataService;

    @Test
    @SuppressWarnings("unchecked")
    void testFetchJobTitle_All() {
        when(roleTypeRepository.findAll())
                .thenReturn(prepareRoleTypeResponse());
        var roleTypes = staffRefDataService
                .getJobTitles();
        verifyAllRoleTypes(roleTypes);
        //added to verify content
        assertTrue(verifyAllRoleTypesContent(roleTypes, prepareRoleTypeResponse()));
        //added to verify counts
        assertEquals(4, roleTypes.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchJobTitle_one() {
        List<RoleType> roleTypes = new ArrayList<>();
        roleTypes.add(new RoleType(1l, "Role Type 1"));
        when(roleTypeRepository.findAll()).thenReturn(roleTypes);
        var staffRefDataUserTypesResponses = (List<RoleType>) staffRefDataService
                .getJobTitles();
        assertFalse(verifyCurrentRoleTypes(staffRefDataUserTypesResponses.get(0)));
        //added to verify counts
        assertEquals(1, staffRefDataUserTypesResponses.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchJobTitle_NoDataFound() {
        var userTypes = prepareRoleTypeResponse();
        userTypes.clear();
        when(roleTypeRepository.findAll()).thenReturn(userTypes);
        var staffRefDataUserTypesResponses = (List<RoleType>) staffRefDataService
                .getJobTitles();
        assertTrue(staffRefDataUserTypesResponses.isEmpty());
        assertEquals(0, staffRefDataUserTypesResponses.size());
    }

    private List<RoleType> prepareRoleTypeResponse() {
        var userTypeArrayList = new ArrayList<RoleType>();
        userTypeArrayList.add(new RoleType(1L, "Role Type 1"));
        userTypeArrayList.add(new RoleType(2L, "Role Type 2"));
        userTypeArrayList.add(new RoleType(3L, "Role Type 3"));
        userTypeArrayList.add(new RoleType(4L, "Role Type 4"));

        return userTypeArrayList;
    }

    private void verifyAllRoleTypes(List<RoleType> roleTypes) {
        boolean isInvalidResponse = roleTypes
                .stream()
                .anyMatch(roleType -> verifyCurrentRoleTypes(roleType));
        assertFalse(isInvalidResponse);
    }

    private boolean verifyCurrentRoleTypes(RoleType roleType) {
        return roleType.getRoleId() == null || roleType.getDescription() == null;
    }

    private boolean verifyAllRoleTypesContent(List<RoleType> roleTypes, List<RoleType> prepareRoleTypeResponse) {
        for (int i = 0; i < prepareRoleTypeResponse.size(); i++) {
            RoleType staffRefDataRoleType = prepareRoleTypeResponse.get(i);
            Optional<RoleType> roleType = roleTypes.stream().filter(e ->
                    e.getRoleId().equals(staffRefDataRoleType.getRoleId())
                            && e.getDescription().equals(staffRefDataRoleType.getDescription())).findAny();
            if (!roleType.isPresent()) {
                return false;
            }
        }
        return true;
    }
}
