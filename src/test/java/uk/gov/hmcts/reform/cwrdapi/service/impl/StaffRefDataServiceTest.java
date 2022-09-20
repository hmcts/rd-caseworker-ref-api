package uk.gov.hmcts.reform.cwrdapi.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
class StaffRefDataServiceTest {

    @Mock
    private UserTypeRepository userTypeRepository;

    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataService;

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_All() {
        when(userTypeRepository.findAll())
            .thenReturn(prepareUserTypeResponse());
        var userTypes =  staffRefDataService
            .fetchUserTypes();
        verifyAllUserTypes(userTypes);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_one() {
        when(userTypeRepository.findAll()).thenReturn(prepareUserTypeResponse());
        var staffRefDataUserTypesResponses = (List<UserType>) staffRefDataService
            .fetchUserTypes();
        assertFalse(verifyCurrentUserTypes(staffRefDataUserTypesResponses.get(0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFetchUserType_NoDataFound() {
        var userTypes = prepareUserTypeResponse();
        userTypes.clear();
        when(userTypeRepository.findAll()).thenReturn(userTypes);
        var staffRefDataUserTypesResponses = (List<UserType>) staffRefDataService
                .fetchUserTypes();
        assertTrue(staffRefDataUserTypesResponses.isEmpty());
    }

    private List<UserType> prepareUserTypeResponse() {
        var userTypeArrayList = new ArrayList<UserType>();
        userTypeArrayList.add(new UserType(1L,"User Type 1"));
        userTypeArrayList.add(new UserType(2L,"User Type 2"));
        userTypeArrayList.add(new UserType(3L,"User Type 3"));
        userTypeArrayList.add(new UserType(4L,"User Type 4"));

        return userTypeArrayList;
    }

    private void verifyAllUserTypes(List<UserType> userTypes) {
        boolean isInvalidResponse = userTypes
                .stream()
                .anyMatch(userType -> verifyCurrentUserTypes(userType));
        assertFalse(isInvalidResponse);
    }

    private boolean verifyCurrentUserTypes(UserType userType) {
        return userType.getUserTypeId()==null || userType.getDescription()==null;
    }
}