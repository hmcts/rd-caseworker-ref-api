package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IStaffProfileAuditService;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import javax.transaction.Transactional;

import static java.util.Objects.nonNull;

@Component
@Slf4j
public class StaffProfileAuditServiceImpl implements IStaffProfileAuditService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    @Lazy
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Autowired
    private StaffAuditRepository staffAuditRepository;

    private static ObjectMapper objectMapper = new ObjectMapper();


    @Async
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveStaffAudit(AuditStatus auditStatus, String errorMessage, String caseWorkerId,
                               StaffProfileCreationRequest staffProfileCreationRequest, String operationType) {
        try {

            if (errorMessage != null && errorMessage.length() > 512) {
                errorMessage = errorMessage.substring(0, 511);
            }

            UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
            String userId = (nonNull(userInfo) && nonNull(userInfo.getUid())) ? userInfo.getUid() : null;
            String request = objectMapper.writeValueAsString(staffProfileCreationRequest);

            StaffAudit staffAudit = StaffAudit.builder()
                    .status(auditStatus.getStatus().toUpperCase())
                    .requestTimeStamp(LocalDateTime.now())
                    .errorDescription(errorMessage)
                    .authenticatedUserId(userId)
                    .caseWorkerId(caseWorkerId)
                    .operationType(operationType)
                    .requestLog(request)
                    .build();

            staffAuditRepository.save(staffAudit);
        } catch (JsonProcessingException e) {
            log.error("{}:: Failure error Message {} in saveStaffAudit {}  ", loggingComponentName, e.getMessage(),
                    caseWorkerId);
        }
    }
}
