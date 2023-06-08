package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfilesDeletionResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImplTest.COMMON_EMAIL_PATTERN;

@ExtendWith(MockitoExtension.class)
class CaseWorkerDeleteServiceImplTest {

    @InjectMocks
    private CaseWorkerDeleteServiceImpl caseWorkerDeleteServiceImpl;

    @Mock
    private UserProfileFeignClient userProfileFeignClient;
    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepository;

    private CaseWorkerProfile caseWorkerProfile;

    @BeforeEach
    void setUp() {
        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");
    }

    @Test
    void testDeleteUserProfileByUserId() {
        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(caseWorkerProfileRepository.findByCaseWorkerId(any(String.class)))
                .thenReturn(Optional.ofNullable(caseWorkerProfile));

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerDeleteServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerId(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
        verify(responseMock, times(2)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    void testDeleteUserProfileByUserId_WhenUpReturns404() {
        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(caseWorkerProfileRepository.findByCaseWorkerId(any(String.class)))
                .thenReturn(Optional.ofNullable(caseWorkerProfile));

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerDeleteServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerId(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
        verify(responseMock, times(3)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    void testDeleteUserProfileByUserId_WhenUpReturnsError() {
        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("UP Delete request failed for userId: " + caseWorkerProfile.getCaseWorkerId()
                + ". With the following UP message: INTERNAL SERVER ERROR");
        deletionResponse.setStatusCode(BAD_REQUEST.value());

        Response responseMock = mock(Response.class);

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(BAD_REQUEST.value());

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerDeleteServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).contains("UP Delete request failed for userId");

        verify(responseMock, times(3)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    void testDeleteUserProfileByEmailPattern() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);

        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(null, COMMON_EMAIL_PATTERN))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(caseWorkerProfileRepository
                .findByEmailIdIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(caseWorkerProfiles);

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerDeleteServiceImpl.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(null, COMMON_EMAIL_PATTERN);
        verify(responseMock, times(1)).status();
        verify(caseWorkerProfileRepository, times(1))
                .findByEmailIdIgnoreCaseContaining(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
    }

    @Test
    void testDeleteUserProfileByEmailPattern_WhenUpReturns404() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);

        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(null, COMMON_EMAIL_PATTERN))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(caseWorkerProfileRepository
                .findByEmailIdIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(caseWorkerProfiles);

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerDeleteServiceImpl.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(null, COMMON_EMAIL_PATTERN);
        verify(responseMock, times(2)).status();
        verify(caseWorkerProfileRepository, times(1))
                .findByEmailIdIgnoreCaseContaining(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
    }

    @ParameterizedTest
    @CsvSource({
        "204,User deleted in UP but was not present in CRD with userId",
        "404,User was not present in UP or CRD with userId"
    })
    @SuppressWarnings("unchecked")
    void testValidateUserAfterUpDeleteWhenStatusIs204(int status,String description) {
        Optional<CaseWorkerProfile> userProfile = mock(Optional.class);
        String userId = UUID.randomUUID().toString();

        when(userProfile.isPresent()).thenReturn(false);

        CaseWorkerProfilesDeletionResponse deletionResponse =
                caseWorkerDeleteServiceImpl.validateUserAfterUpDelete(userProfile, userId, status);

        assertThat(deletionResponse.getStatusCode()).isEqualTo(status);
        assertThat(deletionResponse.getMessage())
                .isEqualTo(description + ": " + userId);

        verify(userProfile, times(1)).isPresent();
    }

}
