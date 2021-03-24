package uk.gov.hmcts.reform.cwrdapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ForbiddenException;
import uk.gov.hmcts.reform.cwrdapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cwrdapi.repository.AuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.FILE_UPLOAD_IN_PROGRESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.IN_PROGRESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;

@MultipartConfig
@Component
@Slf4j
public class AuditInterceptor implements HandlerInterceptor {

    @Autowired
    IValidationService validationServiceFacadeImpl;

    @Autowired
    AuditRepository auditRepository;

    @Autowired
    @Lazy
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Value("${logging-component-name}")
    private String loggingComponentName;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object handler) {

        MultipartFile multipartFile = ((MultipartHttpServletRequest) request).getFile(FILE);
        if (nonNull(multipartFile)) {
            long audits = auditRepository.findByAuthenticatedUserIdAndStatus(
                jwtGrantedAuthoritiesConverter.getUserInfo().getUid(), IN_PROGRESS.getStatus(),
                multipartFile.getOriginalFilename());

            if (audits == 0) {
                //Starts CWR Auditing with Job Status in Progress.
                long jobId = validationServiceFacadeImpl.startCaseworkerAuditing(IN_PROGRESS,
                    multipartFile.getOriginalFilename());
                log.info("{}:: Started File Upload with job {}", loggingComponentName, jobId);
            } else {
                throw new ForbiddenException(FILE_UPLOAD_IN_PROGRESS.getErrorMessage());
            }
        } else {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NO_DATA_ERROR_MESSAGE);
        }
        return true;
    }
}
